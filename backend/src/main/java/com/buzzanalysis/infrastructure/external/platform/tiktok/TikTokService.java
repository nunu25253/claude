package com.buzzanalysis.infrastructure.external.platform.tiktok;

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
 * TikTok向け {@link SocialPlatform} 実装。
 * <p><b>本番実装への置き換えポイント：</b>実際には TikTok Display API / Research API の
 * {@code /v2/video/query/} エンドポイントにOAuthアクセストークンを付与して呼び出す。
 * アクセストークン（{@code sns.tiktok.access-token}）が未設定の場合はモックデータへフォールバックする。</p>
 */
@Component
public class TikTokService implements SocialPlatform {

    private static final Logger log = LoggerFactory.getLogger(TikTokService.class);
    private static final Pattern VIDEO_ID_PATTERN = Pattern.compile("tiktok\\.com/@[\\w.-]+/video/(\\d+)");

    private final TikTokApiProperties properties;
    private final WebClient webClient;

    public TikTokService(TikTokApiProperties properties, WebClient.Builder webClientBuilder) {
        this.properties = properties;
        this.webClient = webClientBuilder.baseUrl(properties.getApiBaseUrl()).build();
    }

    @Override
    public Platform platform() {
        return Platform.TIKTOK;
    }

    @Override
    public Optional<FetchedPostData> fetchPost(String postUrlOrId) {
        String videoId = extractVideoId(postUrlOrId);

        if (!properties.isConfigured()) {
            log.info("TikTok access token not configured; returning stub data for {}", postUrlOrId);
            return Optional.of(stubPost(videoId, postUrlOrId));
        }
        try {
            // 本番では POST /v2/video/query/ に video_id を指定し、
            // like_count / comment_count / share_count / view_count 等のpublic_metricsを取得する。
            log.warn("Live TikTok Display API call is not implemented in this environment; falling back to stub data.");
            return Optional.of(stubPost(videoId, postUrlOrId));
        } catch (Exception e) {
            throw new ExternalApiException("Failed to fetch TikTok post: " + postUrlOrId, e);
        }
    }

    @Override
    public Optional<FetchedAccountData> fetchAccount(String usernameOrId) {
        if (!properties.isConfigured()) {
            log.info("TikTok access token not configured; returning stub account data for {}", usernameOrId);
            return Optional.of(stubAccount(usernameOrId));
        }
        try {
            // 本番では GET /v2/user/info/ を呼び出す。
            log.warn("Live TikTok Display API call is not implemented in this environment; falling back to stub data.");
            return Optional.of(stubAccount(usernameOrId));
        } catch (Exception e) {
            throw new ExternalApiException("Failed to fetch TikTok account: " + usernameOrId, e);
        }
    }

    private String extractVideoId(String postUrlOrId) {
        Matcher matcher = VIDEO_ID_PATTERN.matcher(postUrlOrId);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return postUrlOrId;
    }

    private FetchedPostData stubPost(String videoId, String originalUrl) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long likes = random.nextLong(10_000, 2_000_000);
        long comments = random.nextLong(100, 20_000);
        long views = random.nextLong(100_000, 10_000_000);
        return new FetchedPostData(
                videoId,
                originalUrl.startsWith("http") ? originalUrl : "https://www.tiktok.com/@demo_tt_creator/video/" + videoId,
                OffsetDateTime.now().minusDays(random.nextInt(1, 20)),
                "demo_tt_creator",
                "これバズるやつ #fyp #おすすめ",
                List.of("fyp", "おすすめ", "trend"),
                likes,
                comments,
                views,
                random.nextLong(500, 50_000),
                random.nextInt(9, 60),
                null,
                PostType.VIDEO
        );
    }

    private FetchedAccountData stubAccount(String username) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new FetchedAccountData(
                username,
                username,
                username,
                "https://www.tiktok.com/@" + username,
                random.nextLong(50_000, 5_000_000),
                random.nextLong(20, 1_000)
        );
    }
}
