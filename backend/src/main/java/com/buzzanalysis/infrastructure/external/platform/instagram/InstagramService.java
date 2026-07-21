package com.buzzanalysis.infrastructure.external.platform.instagram;

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
 * Instagram向け {@link SocialPlatform} 実装。
 * <p>
 * Instagram Graph APIの<b>Business Discovery</b>（{@code /{ig-business-id}?fields=business_discovery...}）を
 * 用いて、自社ビジネス/クリエイターアカウントのトークンで“他の”ビジネス/クリエイターアカウントの公開データ
 * （プロフィール・直近投稿の公開指標）を取得する。Business Discoveryは仕様上「ユーザー名を指定して直近の
 * メディア一覧」しか返せず、任意の投稿URLをキーに1件だけ取得することはできないため、単一投稿分析
 * （{@link #fetchPost}）はoEmbedで投稿者名を特定した上でBusiness Discoveryの直近メディア一覧と突き合わせる
 * 2段階の実装とする（対象アカウントの直近{@value #RECENT_MEDIA_LOOKUP_LIMIT}件より古い投稿は取得不可）。
 * </p>
 * <p>アクセストークン・ビジネスアカウントID（{@code sns.instagram.access-token}/{@code business-account-id}）が
 * 未設定の場合は、開発・デモ用にモックデータへフォールバックする。</p>
 */
@Component
public class InstagramService implements SocialPlatform {

    private static final Logger log = LoggerFactory.getLogger(InstagramService.class);
    private static final Pattern SHORTCODE_PATTERN = Pattern.compile("instagram\\.com/(?:p|reel|tv)/([A-Za-z0-9_-]+)");
    private static final int RECENT_MEDIA_LOOKUP_LIMIT = 50;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private final InstagramApiProperties properties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final HashtagExtractor hashtagExtractor;

    public InstagramService(InstagramApiProperties properties, WebClient.Builder webClientBuilder,
                             ObjectMapper objectMapper, HashtagExtractor hashtagExtractor) {
        this.properties = properties;
        this.webClient = webClientBuilder.baseUrl(properties.getGraphApiBaseUrl()).build();
        this.objectMapper = objectMapper;
        this.hashtagExtractor = hashtagExtractor;
    }

    @Override
    public Platform platform() {
        return Platform.INSTAGRAM;
    }

    @Override
    public Optional<FetchedPostData> fetchPost(String postUrlOrId) {
        String shortcode = extractShortcode(postUrlOrId);

        if (!properties.isConfigured()) {
            log.info("Instagram access token not configured; returning stub data for {}", postUrlOrId);
            return Optional.of(stubPost(shortcode, postUrlOrId));
        }

        try {
            // oEmbedはアクセストークンさえあれば任意の公開投稿URLから投稿者名を取得できる（認証済みトークンが必要、
            // 2020年のAPI仕様変更以降は無認証では利用不可）。ただしいいね数・コメント数等の公開指標は含まれない。
            JsonNode oEmbed = callGraphApi("/instagram_oembed", "url", postUrlOrId, "access_token", properties.getAccessToken());
            String authorUsername = oEmbed.path("author_name").asText(null);
            if (authorUsername == null) {
                throw new ExternalApiException("Instagram oEmbed did not return an author for: " + postUrlOrId);
            }

            // 投稿者の直近メディア一覧（Business Discovery）から、URLのshortcodeに一致する投稿を探して
            // 公開指標（いいね数・コメント数等）を補完する。
            JsonNode media = fetchBusinessDiscoveryMedia(authorUsername, RECENT_MEDIA_LOOKUP_LIMIT);
            for (JsonNode item : media) {
                String permalink = item.path("permalink").asText("");
                if (permalink.contains(shortcode)) {
                    return Optional.of(toFetchedPostData(item, authorUsername));
                }
            }

            log.warn("Instagram Business Discovery only exposes an account's most recent {} media items; "
                            + "post (author={}) was not found within that window (likely older, or the account "
                            + "is not a Business/Creator account); falling back to stub data.",
                    RECENT_MEDIA_LOOKUP_LIMIT, authorUsername);
            return Optional.of(stubPost(shortcode, postUrlOrId));
        } catch (Exception e) {
            log.warn("Instagram Graph API call failed for {}; falling back to stub data.", postUrlOrId, e);
            return Optional.of(stubPost(shortcode, postUrlOrId));
        }
    }

    @Override
    public Optional<FetchedAccountData> fetchAccount(String usernameOrId) {
        if (!properties.isConfigured()) {
            log.info("Instagram access token not configured; returning stub account data for {}", usernameOrId);
            return Optional.of(stubAccount(usernameOrId));
        }
        try {
            JsonNode discovery = callGraphApi("/" + properties.getBusinessAccountId(),
                    "fields", "business_discovery.username(" + usernameOrId + "){id,username,followers_count,media_count}",
                    "access_token", properties.getAccessToken())
                    .path("business_discovery");
            if (discovery.isMissingNode()) {
                return Optional.empty();
            }
            return Optional.of(new FetchedAccountData(
                    discovery.path("id").asText(usernameOrId),
                    discovery.path("username").asText(usernameOrId),
                    discovery.path("username").asText(usernameOrId),
                    "https://www.instagram.com/" + usernameOrId + "/",
                    discovery.hasNonNull("followers_count") ? discovery.get("followers_count").asLong() : null,
                    discovery.hasNonNull("media_count") ? discovery.get("media_count").asLong() : null
            ));
        } catch (Exception e) {
            log.warn("Instagram Graph API call failed for account {}; falling back to stub data.", usernameOrId, e);
            return Optional.of(stubAccount(usernameOrId));
        }
    }

    @Override
    public List<FetchedPostData> fetchRecentPosts(String usernameOrId, int limit) {
        if (!properties.isConfigured()) {
            log.info("Instagram access token not configured; returning stub recent posts for {}", usernameOrId);
            return stubRecentPosts(usernameOrId, limit);
        }
        try {
            JsonNode media = fetchBusinessDiscoveryMedia(usernameOrId, limit);
            List<FetchedPostData> posts = new java.util.ArrayList<>();
            for (JsonNode item : media) {
                posts.add(toFetchedPostData(item, usernameOrId));
            }
            return posts;
        } catch (Exception e) {
            log.warn("Instagram Graph API call failed for recent posts of {}; falling back to stub data.",
                    usernameOrId, e);
            return stubRecentPosts(usernameOrId, limit);
        }
    }

    /** Business Discovery APIで指定ユーザーの直近メディア一覧（{@code media.data[]}）を取得する。 */
    private JsonNode fetchBusinessDiscoveryMedia(String username, int limit) {
        JsonNode response = callGraphApi("/" + properties.getBusinessAccountId(),
                "fields", "business_discovery.username(" + username + "){media.limit(" + Math.max(1, limit)
                        + "){id,caption,media_type,media_url,permalink,timestamp,like_count,comments_count}}",
                "access_token", properties.getAccessToken());
        return response.path("business_discovery").path("media").path("data");
    }

    private FetchedPostData toFetchedPostData(JsonNode media, String authorUsername) {
        String caption = media.path("caption").asText("");
        return new FetchedPostData(
                media.path("id").asText(),
                media.path("permalink").asText(),
                parseTimestamp(media.path("timestamp").asText(null)),
                authorUsername,
                caption,
                hashtagExtractor.extract(caption),
                media.hasNonNull("like_count") ? media.get("like_count").asLong() : null,
                media.hasNonNull("comments_count") ? media.get("comments_count").asLong() : null,
                // Business DiscoveryはVIDEO/REELの再生数・シェア数を公開データとして返さないため常にnull。
                null,
                null,
                // 動画の長さも標準フィールドにないため常にnull。
                null,
                // カルーセル子要素数(imageCount)もBusiness Discoveryの標準フィールドにないため常にnull。
                // 正確な枚数が必要な場合は children{id} フィールドを追加リクエストする必要がある。
                null,
                toPostType(media.path("media_type").asText(""))
        );
    }

    private PostType toPostType(String mediaType) {
        return switch (mediaType) {
            case "VIDEO" -> PostType.REEL;
            case "CAROUSEL_ALBUM" -> PostType.CAROUSEL;
            default -> PostType.IMAGE;
        };
    }

    private OffsetDateTime parseTimestamp(String timestamp) {
        if (timestamp == null) {
            return null;
        }
        return OffsetDateTime.parse(timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private JsonNode callGraphApi(String path, String... queryParams) {
        String json = webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path(path);
                    for (int i = 0; i < queryParams.length; i += 2) {
                        builder = builder.queryParam(queryParams[i], queryParams[i + 1]);
                    }
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(String.class)
                .timeout(REQUEST_TIMEOUT)
                .block();
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new ExternalApiException("Failed to parse Instagram Graph API response", e);
        }
    }

    private String extractShortcode(String postUrlOrId) {
        Matcher matcher = SHORTCODE_PATTERN.matcher(postUrlOrId);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return postUrlOrId;
    }

    private FetchedPostData stubPost(String shortcode, String originalUrl) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long likes = random.nextLong(5_000, 500_000);
        long comments = random.nextLong(50, 8_000);
        long views = random.nextLong(50_000, 3_000_000);
        return new FetchedPostData(
                shortcode,
                originalUrl.startsWith("http") ? originalUrl : "https://www.instagram.com/reel/" + shortcode + "/",
                OffsetDateTime.now().minusDays(random.nextInt(1, 30)),
                "demo_ig_creator",
                "日常の一コマをシェア！あなたのお気に入りはどれ？\n\n#Reels #トレンド",
                List.of("Reels", "トレンド", "instagood"),
                likes,
                comments,
                views,
                random.nextLong(10, 3_000),
                random.nextInt(8, 45),
                null,
                PostType.REEL
        );
    }

    /** 同一アカウントの再取得で同じ投稿IDを返すよう、ユーザー名+連番で決定的な externalId を生成する。 */
    private List<FetchedPostData> stubRecentPosts(String usernameOrId, int limit) {
        List<FetchedPostData> posts = new java.util.ArrayList<>();
        for (int i = 0; i < Math.max(0, limit); i++) {
            String shortcode = usernameOrId + "-ig-" + i;
            FetchedPostData base = stubPost(shortcode, "https://www.instagram.com/reel/" + shortcode + "/");
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
                "https://www.instagram.com/" + username + "/",
                random.nextLong(10_000, 2_000_000),
                random.nextLong(50, 3_000)
        );
    }
}
