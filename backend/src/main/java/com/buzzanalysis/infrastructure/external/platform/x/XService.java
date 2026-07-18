package com.buzzanalysis.infrastructure.external.platform.x;

import com.buzzanalysis.domain.common.exception.ExternalApiException;
import com.buzzanalysis.domain.platform.FetchedAccountData;
import com.buzzanalysis.domain.platform.FetchedPostData;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.platform.SocialPlatform;
import com.buzzanalysis.domain.post.PostType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * X (旧Twitter) 向け {@link SocialPlatform} 実装。
 * <p><b>本番実装への置き換えポイント：</b>実際には X API v2 の
 * {@code GET /2/tweets/:id?tweet.fields=public_metrics,created_at,attachments} エンドポイントに
 * Bearer Tokenを付与して呼び出す。トークン（{@code sns.x.bearer-token}）が未設定の場合はモックデータへフォールバックする。</p>
 */
@Component
public class XService implements SocialPlatform {

    private static final Logger log = LoggerFactory.getLogger(XService.class);
    private static final Pattern STATUS_ID_PATTERN = Pattern.compile("(?:x|twitter)\\.com/[\\w]+/status/(\\d+)");

    private final XApiProperties properties;
    private final WebClient webClient;

    public XService(XApiProperties properties, WebClient.Builder webClientBuilder) {
        this.properties = properties;
        this.webClient = webClientBuilder.baseUrl(properties.getApiBaseUrl()).build();
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
            // 本番では GET /2/tweets/{id}?tweet.fields=public_metrics,created_at,author_id を呼び出す。
            log.warn("Live X API call is not implemented in this environment; falling back to stub data.");
            return Optional.of(stubPost(tweetId, postUrlOrId));
        } catch (Exception e) {
            throw new ExternalApiException("Failed to fetch X post: " + postUrlOrId, e);
        }
    }

    @Override
    public Optional<FetchedAccountData> fetchAccount(String usernameOrId) {
        if (!properties.isConfigured()) {
            log.info("X bearer token not configured; returning stub account data for {}", usernameOrId);
            return Optional.of(stubAccount(usernameOrId));
        }
        try {
            // 本番では GET /2/users/by/username/{username}?user.fields=public_metrics を呼び出す。
            log.warn("Live X API call is not implemented in this environment; falling back to stub data.");
            return Optional.of(stubAccount(usernameOrId));
        } catch (Exception e) {
            throw new ExternalApiException("Failed to fetch X account: " + usernameOrId, e);
        }
    }

    @Override
    public List<FetchedPostData> fetchRecentPosts(String usernameOrId, int limit) {
        if (!properties.isConfigured()) {
            log.info("X bearer token not configured; returning stub recent posts for {}", usernameOrId);
            return stubRecentPosts(usernameOrId, limit);
        }
        try {
            // 本番では GET /2/users/by/username/{username}/tweets?tweet.fields=public_metrics を呼び出す。
            log.warn("Live X API call is not implemented in this environment; falling back to stub data.");
            return stubRecentPosts(usernameOrId, limit);
        } catch (Exception e) {
            throw new ExternalApiException("Failed to fetch X recent posts: " + usernameOrId, e);
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
