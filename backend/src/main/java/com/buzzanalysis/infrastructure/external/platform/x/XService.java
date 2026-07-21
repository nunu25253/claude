package com.buzzanalysis.infrastructure.external.platform.x;

import com.buzzanalysis.domain.common.exception.ExternalApiException;
import com.buzzanalysis.domain.platform.FetchedAccountData;
import com.buzzanalysis.domain.platform.FetchedPostData;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.platform.SocialPlatform;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.preprocessing.HashtagExtractor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * X (旧Twitter) 向け {@link SocialPlatform} 実装。
 * <p>
 * X API v2にBearer Token（App-onlyアクセストークン）を付与して呼び出す。App-only認証はInstagramの
 * Business Discoveryと同様、自分のトークンで任意の公開アカウント/投稿を閲覧できる
 * （TikTokと異なり、対象アカウント側の個別OAuth同意は不要）。
 * ただし読み取り系エンドポイント（ユーザー検索・投稿検索・タイムライン取得）はXの無料プラン(Free tier)では
 * 利用できず、最低でもBasicプラン以上の契約が必要（2024年時点の仕様。詳細はX APIの料金ページを参照）。
 * トークン（{@code sns.x.bearer-token}）が未設定の場合はモックデータへフォールバックする。</p>
 */
@Component
public class XService implements SocialPlatform {

    private static final Logger log = LoggerFactory.getLogger(XService.class);
    private static final Pattern STATUS_ID_PATTERN = Pattern.compile("(?:x|twitter)\\.com/[\\w]+/status/(\\d+)");
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final String TWEET_FIELDS = "public_metrics,created_at,attachments";
    private static final String USER_FIELDS = "public_metrics";

    private final XApiProperties properties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final HashtagExtractor hashtagExtractor;

    public XService(XApiProperties properties, WebClient.Builder webClientBuilder,
                     ObjectMapper objectMapper, HashtagExtractor hashtagExtractor) {
        this.properties = properties;
        this.webClient = webClientBuilder.baseUrl(properties.getApiBaseUrl()).build();
        this.objectMapper = objectMapper;
        this.hashtagExtractor = hashtagExtractor;
    }

    @Override
    public Platform platform() {
        return Platform.X;
    }

    @Override
    public Optional<FetchedPostData> fetchPost(String postUrlOrId) {
        String tweetId = extractTweetId(postUrlOrId);

        if (!properties.isConfigured()) {
            log.info("X bearer token not configured; returning stub data for {}", postUrlOrId);
            return Optional.of(stubPost(tweetId, postUrlOrId));
        }
        try {
            JsonNode response = callApi("/tweets/" + tweetId,
                    "tweet.fields", TWEET_FIELDS, "expansions", "author_id", "user.fields", "username");
            JsonNode tweet = response.path("data");
            if (tweet.isMissingNode()) {
                return Optional.empty();
            }
            String authorId = tweet.path("author_id").asText(null);
            String authorUsername = resolveUsernameFromIncludes(response, authorId);
            return Optional.of(toFetchedPostData(tweet, authorUsername, postUrlOrId));
        } catch (Exception e) {
            // Free tierでは読み取り系エンドポイント自体が使えない(403)等、トークンはあっても実際には
            // 呼び出せないケースが多いため、ハードエラーにはせずスタブデータへフォールバックする。
            log.warn("X API call failed for {}; falling back to stub data. "
                    + "This commonly happens on the X API Free tier, which does not include read access.",
                    postUrlOrId, e);
            return Optional.of(stubPost(tweetId, postUrlOrId));
        }
    }

    @Override
    public Optional<FetchedAccountData> fetchAccount(String usernameOrId) {
        if (!properties.isConfigured()) {
            log.info("X bearer token not configured; returning stub account data for {}", usernameOrId);
            return Optional.of(stubAccount(usernameOrId));
        }
        try {
            JsonNode user = fetchUserByUsername(usernameOrId);
            if (user.isMissingNode()) {
                return Optional.empty();
            }
            JsonNode metrics = user.path("public_metrics");
            return Optional.of(new FetchedAccountData(
                    user.path("id").asText(usernameOrId),
                    user.path("username").asText(usernameOrId),
                    user.path("name").asText(usernameOrId),
                    "https://x.com/" + user.path("username").asText(usernameOrId),
                    metrics.hasNonNull("followers_count") ? metrics.get("followers_count").asLong() : null,
                    metrics.hasNonNull("tweet_count") ? metrics.get("tweet_count").asLong() : null
            ));
        } catch (Exception e) {
            log.warn("X API call failed for account {}; falling back to stub data. "
                    + "This commonly happens on the X API Free tier, which does not include read access.",
                    usernameOrId, e);
            return Optional.of(stubAccount(usernameOrId));
        }
    }

    @Override
    public List<FetchedPostData> fetchRecentPosts(String usernameOrId, int limit) {
        if (!properties.isConfigured()) {
            log.info("X bearer token not configured; returning stub recent posts for {}", usernameOrId);
            return stubRecentPosts(usernameOrId, limit);
        }
        try {
            JsonNode user = fetchUserByUsername(usernameOrId);
            if (user.isMissingNode()) {
                return List.of();
            }
            String userId = user.path("id").asText();
            // max_resultsは5〜100の範囲でしか指定できない仕様のためクランプする。
            int maxResults = Math.min(100, Math.max(5, limit));
            JsonNode response = callApi("/users/" + userId + "/tweets",
                    "max_results", String.valueOf(maxResults),
                    "tweet.fields", TWEET_FIELDS,
                    "exclude", "retweets,replies");
            List<FetchedPostData> posts = new java.util.ArrayList<>();
            for (JsonNode tweet : response.path("data")) {
                String tweetId = tweet.path("id").asText();
                posts.add(toFetchedPostData(tweet, usernameOrId, "https://x.com/" + usernameOrId + "/status/" + tweetId));
                if (posts.size() >= limit) {
                    break;
                }
            }
            return posts;
        } catch (Exception e) {
            log.warn("X API call failed for recent posts of {}; falling back to stub data. "
                    + "This commonly happens on the X API Free tier, which does not include read access.",
                    usernameOrId, e);
            return stubRecentPosts(usernameOrId, limit);
        }
    }

    private JsonNode fetchUserByUsername(String username) {
        return callApi("/users/by/username/" + username, "user.fields", USER_FIELDS).path("data");
    }

    private String resolveUsernameFromIncludes(JsonNode response, String authorId) {
        if (authorId == null) {
            return "unknown";
        }
        for (JsonNode user : response.path("includes").path("users")) {
            if (authorId.equals(user.path("id").asText())) {
                return user.path("username").asText(authorId);
            }
        }
        return authorId;
    }

    private FetchedPostData toFetchedPostData(JsonNode tweet, String authorUsername, String url) {
        String text = tweet.path("text").asText("");
        JsonNode metrics = tweet.path("public_metrics");
        String createdAt = tweet.path("created_at").asText(null);
        return new FetchedPostData(
                tweet.path("id").asText(),
                url,
                createdAt != null ? OffsetDateTime.parse(createdAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null,
                authorUsername,
                text,
                hashtagExtractor.extract(text),
                metrics.hasNonNull("like_count") ? metrics.get("like_count").asLong() : null,
                metrics.hasNonNull("reply_count") ? metrics.get("reply_count").asLong() : null,
                // impression_countはアクセスレベルによって含まれないことがあるためnull許容。
                metrics.hasNonNull("impression_count") ? metrics.get("impression_count").asLong() : null,
                metrics.hasNonNull("retweet_count") ? metrics.get("retweet_count").asLong() : null,
                null,
                null,
                PostType.TEXT
        );
    }

    private JsonNode callApi(String path, String... queryParams) {
        // apiBaseUrl(既定 https://api.twitter.com/2)は末尾に既に "/2" を含むため、ここではpathをそのまま付与する。
        String json = webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path(path);
                    for (int i = 0; i < queryParams.length; i += 2) {
                        builder = builder.queryParam(queryParams[i], queryParams[i + 1]);
                    }
                    return builder.build();
                })
                .header("Authorization", "Bearer " + properties.getBearerToken())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(REQUEST_TIMEOUT)
                .block();
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new ExternalApiException("Failed to parse X API response", e);
        }
    }

    private String extractTweetId(String postUrlOrId) {
        Matcher matcher = STATUS_ID_PATTERN.matcher(postUrlOrId);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return postUrlOrId;
    }

    private FetchedPostData stubPost(String tweetId, String originalUrl) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long likes = random.nextLong(1_000, 200_000);
        long comments = random.nextLong(20, 5_000);
        return new FetchedPostData(
                tweetId,
                originalUrl.startsWith("http") ? originalUrl : "https://x.com/demo_x_user/status/" + tweetId,
                OffsetDateTime.now().minusDays(random.nextInt(1, 14)),
                "demo_x_user",
                "これは伸びそうな予感がする投稿です。 #バズった #トレンド入り",
                List.of("バズった", "トレンド入り"),
                likes,
                comments,
                random.nextLong(50_000, 5_000_000),
                random.nextLong(500, 30_000),
                null,
                null,
                PostType.TEXT
        );
    }

    /** 同一アカウントの再取得で同じ投稿IDを返すよう、ユーザー名+連番で決定的な externalId を生成する。 */
    private List<FetchedPostData> stubRecentPosts(String usernameOrId, int limit) {
        List<FetchedPostData> posts = new java.util.ArrayList<>();
        for (int i = 0; i < Math.max(0, limit); i++) {
            String tweetId = usernameOrId + "-x-" + i;
            FetchedPostData base = stubPost(tweetId, "https://x.com/" + usernameOrId + "/status/" + tweetId);
            posts.add(new FetchedPostData(
                    base.externalId(), base.url(), OffsetDateTime.now().minusDays(i + 1L), usernameOrId,
                    base.caption(), base.hashtags(), base.likeCount(), base.commentCount(), base.viewCount(),
                    base.shareCount(), base.videoDurationSeconds(), base.imageCount(), base.postType()
            ));
        }
        return posts;
    }

    private FetchedAccountData stubAccount(String username) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new FetchedAccountData(
                username,
                username,
                username,
                "https://x.com/" + username,
                random.nextLong(5_000, 1_000_000),
                random.nextLong(100, 20_000)
        );
    }
}
