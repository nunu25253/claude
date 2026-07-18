package com.buzzanalysis.infrastructure.external.platform.tiktok;

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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TikTok向け {@link SocialPlatform} 実装。
 * <p>
 * {@link #fetchPost}はTikTokの<b>oEmbed</b>（{@code GET https://www.tiktok.com/oembed?url=...}）を使う。
 * oEmbedは認証不要で任意の公開動画URLから投稿者名・タイトル・サムネイルを取得できるが、
 * いいね数・コメント数・再生数などの公開指標は含まれない（TikTokの公開APIはoEmbedではエンゲージメント指標を
 * 提供していないため、engagement系フィールドは常にnullになる）。
 * </p>
 * <p>
 * {@link #fetchAccount}/{@link #fetchRecentPosts}はTikTok for Developersの<b>Display API v2</b>
 * （{@code /v2/user/info/}・{@code /v2/video/list/}）を使う。これらはOAuth（Login Kit）でユーザー本人が
 * 認可したアクセストークンでしか呼び出せず、Instagram Business Discoveryのように任意の第三者アカウントを
 * 指定して閲覧することはできない仕様のため、{@code usernameOrId}引数に関わらず
 * <b>トークンの認可元アカウント自身のデータ</b>が返る（第三者アカウント＝競合アカウントの動画一覧取得には
 * 使えない。これはこのアプリの実装上の制約ではなくTikTok公式APIの仕様上の制約）。
 * アクセストークン（{@code sns.tiktok.access-token}）が未設定の場合はモックデータへフォールバックする。</p>
 */
@Component
public class TikTokService implements SocialPlatform {

    private static final Logger log = LoggerFactory.getLogger(TikTokService.class);
    private static final Pattern VIDEO_ID_PATTERN = Pattern.compile("tiktok\\.com/@[\\w.-]+/video/(\\d+)");
    private static final String OEMBED_BASE_URL = "https://www.tiktok.com";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private final TikTokApiProperties properties;
    private final WebClient webClient;
    private final WebClient oEmbedClient;
    private final ObjectMapper objectMapper;
    private final HashtagExtractor hashtagExtractor;

    public TikTokService(TikTokApiProperties properties, WebClient.Builder webClientBuilder,
                          ObjectMapper objectMapper, HashtagExtractor hashtagExtractor) {
        this.properties = properties;
        this.webClient = webClientBuilder.baseUrl(properties.getApiBaseUrl()).build();
        // oEmbedはAPIベースURLと異なるホスト(www.tiktok.com)のため、注入されたBuilderとは別に組み立てる。
        this.oEmbedClient = WebClient.builder().baseUrl(OEMBED_BASE_URL).build();
        this.objectMapper = objectMapper;
        this.hashtagExtractor = hashtagExtractor;
    }

    @Override
    public Platform platform() {
        return Platform.TIKTOK;
    }

    @Override
    public Optional<FetchedPostData> fetchPost(String postUrlOrId) {
        String videoId = extractVideoId(postUrlOrId);
        String url = postUrlOrId.startsWith("http") ? postUrlOrId
                : "https://www.tiktok.com/@unknown/video/" + videoId;

        try {
            String json = oEmbedClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/oembed").queryParam("url", url).build())
                    // User-Agent無しのリクエストは403で拒否されることがあるため、ブラウザ相当のUAを付与する。
                    .header("User-Agent", "Mozilla/5.0 (compatible; BuzzAnalysisBot/1.0; +https://buzzanalysis.example.com)")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(REQUEST_TIMEOUT)
                    .block();
            JsonNode node = objectMapper.readTree(json);
            String title = node.path("title").asText("");
            String authorName = node.path("author_name").asText("unknown");
            return Optional.of(new FetchedPostData(
                    videoId,
                    url,
                    null, // oEmbedは投稿日時を返さない
                    authorName,
                    title,
                    hashtagExtractor.extract(title),
                    null, null, null, null, null, null, // oEmbedはエンゲージメント指標を返さない
                    PostType.VIDEO
            ));
        } catch (Exception e) {
            log.warn("TikTok oEmbed lookup failed for {}; falling back to stub data.", postUrlOrId, e);
            return Optional.of(stubPost(videoId, postUrlOrId));
        }
    }

    @Override
    public Optional<FetchedAccountData> fetchAccount(String usernameOrId) {
        if (!properties.isConfigured()) {
            log.info("TikTok access token not configured; returning stub account data for {}", usernameOrId);
            return Optional.of(stubAccount(usernameOrId));
        }
        try {
            String fields = "open_id,display_name,avatar_url,follower_count,following_count,likes_count,video_count";
            String json = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/user/info/").queryParam("fields", fields).build())
                    .header("Authorization", "Bearer " + properties.getAccessToken())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(REQUEST_TIMEOUT)
                    .block();
            JsonNode user = objectMapper.readTree(json).path("data").path("user");
            if (user.isMissingNode()) {
                throw new ExternalApiException("TikTok user/info returned no data for token owner");
            }
            String openId = user.path("open_id").asText(usernameOrId);
            return Optional.of(new FetchedAccountData(
                    openId,
                    user.path("display_name").asText(usernameOrId),
                    user.path("display_name").asText(usernameOrId),
                    "https://www.tiktok.com/@" + usernameOrId,
                    user.hasNonNull("follower_count") ? user.get("follower_count").asLong() : null,
                    user.hasNonNull("video_count") ? user.get("video_count").asLong() : null
            ));
        } catch (Exception e) {
            throw new ExternalApiException("Failed to fetch TikTok account: " + usernameOrId, e);
        }
    }

    @Override
    public List<FetchedPostData> fetchRecentPosts(String usernameOrId, int limit) {
        if (!properties.isConfigured()) {
            log.info("TikTok access token not configured; returning stub recent posts for {}", usernameOrId);
            return stubRecentPosts(usernameOrId, limit);
        }
        try {
            String fields = "id,video_description,duration,create_time,share_url,view_count,like_count,comment_count,share_count";
            String json = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/video/list/").queryParam("fields", fields).build())
                    .header("Authorization", "Bearer " + properties.getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("max_count", Math.max(1, limit)))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(REQUEST_TIMEOUT)
                    .block();
            JsonNode videos = objectMapper.readTree(json).path("data").path("videos");
            List<FetchedPostData> posts = new java.util.ArrayList<>();
            for (JsonNode video : videos) {
                String description = video.path("video_description").asText("");
                posts.add(new FetchedPostData(
                        video.path("id").asText(),
                        video.path("share_url").asText(),
                        video.hasNonNull("create_time")
                                ? OffsetDateTime.ofInstant(Instant.ofEpochSecond(video.get("create_time").asLong()), ZoneOffset.UTC)
                                : null,
                        usernameOrId,
                        description,
                        hashtagExtractor.extract(description),
                        video.hasNonNull("like_count") ? video.get("like_count").asLong() : null,
                        video.hasNonNull("comment_count") ? video.get("comment_count").asLong() : null,
                        video.hasNonNull("view_count") ? video.get("view_count").asLong() : null,
                        video.hasNonNull("share_count") ? video.get("share_count").asLong() : null,
                        video.hasNonNull("duration") ? video.get("duration").asInt() : null,
                        null,
                        PostType.VIDEO
                ));
            }
            return posts;
        } catch (Exception e) {
            throw new ExternalApiException("Failed to fetch TikTok recent posts: " + usernameOrId, e);
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

    /** 同一アカウントの再取得で同じ投稿IDを返すよう、ユーザー名+連番で決定的な externalId を生成する。 */
    private List<FetchedPostData> stubRecentPosts(String usernameOrId, int limit) {
        List<FetchedPostData> posts = new java.util.ArrayList<>();
        for (int i = 0; i < Math.max(0, limit); i++) {
            String videoId = usernameOrId + "-tt-" + i;
            FetchedPostData base = stubPost(videoId, "https://www.tiktok.com/@" + usernameOrId + "/video/" + videoId);
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
                "https://www.tiktok.com/@" + username,
                random.nextLong(50_000, 5_000_000),
                random.nextLong(20, 1_000)
        );
    }
}
