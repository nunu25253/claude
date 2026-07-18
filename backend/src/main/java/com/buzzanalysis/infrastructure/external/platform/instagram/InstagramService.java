package com.buzzanalysis.infrastructure.external.platform.instagram;

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
 * Instagram向け {@link SocialPlatform} 実装。
 * <p><b>本番実装への置き換えポイント：</b>実際には Instagram Graph API の
 * Business Discovery（{@code /{ig-business-id}?fields=business_discovery.username(...)} ）や
 * oEmbed APIを用いて公開投稿・アカウント情報を取得する。アクセストークン（{@code sns.instagram.access-token}）が
 * 未設定の場合は、開発・デモ用にモックデータへフォールバックする。</p>
 */
@Component
public class InstagramService implements SocialPlatform {

    private static final Logger log = LoggerFactory.getLogger(InstagramService.class);
    private static final Pattern SHORTCODE_PATTERN = Pattern.compile("instagram\\.com/(?:p|reel|tv)/([A-Za-z0-9_-]+)");

    private final InstagramApiProperties properties;
    private final WebClient webClient;

    public InstagramService(InstagramApiProperties properties, WebClient.Builder webClientBuilder) {
        this.properties = properties;
        this.webClient = webClientBuilder.baseUrl(properties.getGraphApiBaseUrl()).build();
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
            // 本番では Graph API の oEmbed / Business Discovery エンドポイントを呼び出す。
            // 例: webClient.get().uri("/instagram_oembed?url={url}&access_token={token}", postUrlOrId, properties.getAccessToken())
            log.warn("Live Instagram Graph API call is not implemented in this environment; falling back to stub data.");
            return Optional.of(stubPost(shortcode, postUrlOrId));
        } catch (Exception e) {
            throw new ExternalApiException("Failed to fetch Instagram post: " + postUrlOrId, e);
        }
    }

    @Override
    public Optional<FetchedAccountData> fetchAccount(String usernameOrId) {
        if (!properties.isConfigured()) {
            log.info("Instagram access token not configured; returning stub account data for {}", usernameOrId);
            return Optional.of(stubAccount(usernameOrId));
        }
        try {
            // 本番では Business Discovery API: /{ig-business-id}?fields=business_discovery.username({username}){...}
            log.warn("Live Instagram Graph API call is not implemented in this environment; falling back to stub data.");
            return Optional.of(stubAccount(usernameOrId));
        } catch (Exception e) {
            throw new ExternalApiException("Failed to fetch Instagram account: " + usernameOrId, e);
        }
    }

    @Override
    public List<FetchedPostData> fetchRecentPosts(String usernameOrId, int limit) {
        if (!properties.isConfigured()) {
            log.info("Instagram access token not configured; returning stub recent posts for {}", usernameOrId);
            return stubRecentPosts(usernameOrId, limit);
        }
        try {
            // 本番では Business Discovery API の media{...} フィールド（公開投稿一覧）を呼び出す。
            log.warn("Live Instagram Graph API call is not implemented in this environment; falling back to stub data.");
            return stubRecentPosts(usernameOrId, limit);
        } catch (Exception e) {
            throw new ExternalApiException("Failed to fetch Instagram recent posts: " + usernameOrId, e);
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
