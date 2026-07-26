package com.buzzanalysis.application.sync;

import com.buzzanalysis.application.sync.dto.SyncSummaryDto;
import com.buzzanalysis.domain.account.SocialAccount;
import com.buzzanalysis.domain.account.SocialAccountRepository;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.genre.GenreNormalizer;
import com.buzzanalysis.domain.platform.FetchedPostData;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.platform.PlatformFactory;
import com.buzzanalysis.domain.platform.SocialPlatform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostSearchCriteria;
import com.buzzanalysis.domain.ranking.Ranking;
import com.buzzanalysis.domain.ranking.RankingRepository;
import com.buzzanalysis.domain.ranking.RankingType;
import com.buzzanalysis.domain.score.BuzzScore;
import com.buzzanalysis.domain.score.BuzzScoreCalculator;
import com.buzzanalysis.domain.score.BuzzScoreInput;
import com.buzzanalysis.domain.score.BuzzScoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 「定期データ取得バッチ」ユースケース（自動化）。
 *
 * <p>追跡対象（{@link SocialAccount#isTrackingEnabled()}）の全アカウントについて、
 * 各SNS公式APIから最新の公開投稿を再取得して {@code Post} を更新し、BuzzScoreを再計算、
 * 最後に急上昇/週間/月間ランキングを再構築する。手動実行（管理API）とスケジューラの両方から
 * 呼び出される、Clean Architectureにおけるアプリケーション層のユースケースである。</p>
 *
 * <p>非公開データ（インプレッション・リーチ・保存数等）は一切取得しない。取得した投稿の
 * AI分析（OpenAI呼び出し）はコスト・レイテンシの観点からこのバッチでは行わず、
 * 公開メトリクスに基づくBuzzScoreの再計算にとどめる（AI分析はユーザーが明示的に
 * 「投稿URL分析」を実行した場合のみ行われる）。</p>
 */
@Service
public class PeriodicSyncApplicationService {

    private static final Logger log = LoggerFactory.getLogger(PeriodicSyncApplicationService.class);

    /** 現状「自動追跡」を提供しているプラットフォーム。将来SNSを追加した場合はここにも追加する。 */
    private static final List<Platform> TRACKED_PLATFORMS = List.of(Platform.INSTAGRAM, Platform.TIKTOK, Platform.X);

    private final SocialAccountRepository socialAccountRepository;
    private final PlatformFactory platformFactory;
    private final PostRepository postRepository;
    private final BuzzScoreCalculator buzzScoreCalculator;
    private final BuzzScoreRepository buzzScoreRepository;
    private final RankingRepository rankingRepository;
    private final CacheManager cacheManager;
    private final AnalysisResultRepository analysisResultRepository;
    private final GenreNormalizer genreNormalizer;

    public PeriodicSyncApplicationService(SocialAccountRepository socialAccountRepository,
                                           PlatformFactory platformFactory,
                                           PostRepository postRepository,
                                           BuzzScoreCalculator buzzScoreCalculator,
                                           BuzzScoreRepository buzzScoreRepository,
                                           RankingRepository rankingRepository,
                                           CacheManager cacheManager,
                                           AnalysisResultRepository analysisResultRepository,
                                           GenreNormalizer genreNormalizer) {
        this.socialAccountRepository = socialAccountRepository;
        this.platformFactory = platformFactory;
        this.postRepository = postRepository;
        this.buzzScoreCalculator = buzzScoreCalculator;
        this.buzzScoreRepository = buzzScoreRepository;
        this.rankingRepository = rankingRepository;
        this.cacheManager = cacheManager;
        this.analysisResultRepository = analysisResultRepository;
        this.genreNormalizer = genreNormalizer;
    }

    /**
     * 追跡対象アカウントの投稿を同期し、ランキングを再構築する。
     *
     * @param postLimitPerAccount     1アカウントあたりに取得する最新投稿の最大件数
     * @param delayBetweenAccountsMs  各アカウント処理の間に挟む待機時間（レート制限対策、0以下なら待機しない）
     * @param rankingCandidatePoolSize ランキング再計算時に対象とする直近投稿の取得件数上限
     * @param rankingTopN             ランキングとして保存する上位件数
     */
    @Transactional
    public SyncSummaryDto syncAll(int postLimitPerAccount, long delayBetweenAccountsMs,
                                   int rankingCandidatePoolSize, int rankingTopN) {
        OffsetDateTime startedAt = OffsetDateTime.now();
        List<SocialAccount> trackedAccounts = socialAccountRepository.findAllTrackingEnabled();
        List<String> errors = new ArrayList<>();
        int syncedPostCount = 0;

        for (SocialAccount account : trackedAccounts) {
            try {
                syncedPostCount += syncAccount(account, postLimitPerAccount);
            } catch (Exception e) {
                String message = "account=" + account.getId() + " (" + account.getPlatform() + "/"
                        + account.getUsername() + "): " + e.getMessage();
                log.warn("Failed to sync social account: {}", message, e);
                errors.add(message);
            }
            sleepPolitely(delayBetweenAccountsMs);
        }

        int updatedRankingCount = refreshRankings(rankingCandidatePoolSize, rankingTopN);
        evictCaches();

        OffsetDateTime finishedAt = OffsetDateTime.now();
        log.info("Periodic sync finished: accounts={} posts={} rankings={} errors={} durationMs={}",
                trackedAccounts.size(), syncedPostCount, updatedRankingCount, errors.size(),
                Duration.between(startedAt, finishedAt).toMillis());

        return new SyncSummaryDto(trackedAccounts.size(), syncedPostCount, updatedRankingCount, errors,
                startedAt, finishedAt);
    }

    /** 1アカウント分の最新投稿を取得し、Post/BuzzScoreを更新する。戻り値は同期した投稿数。 */
    private int syncAccount(SocialAccount account, int postLimitPerAccount) {
        SocialPlatform client = platformFactory.resolve(account.getPlatform());
        List<FetchedPostData> fetchedPosts = client.fetchRecentPosts(account.getUsername(), postLimitPerAccount);

        int count = 0;
        for (FetchedPostData fetchedPost : fetchedPosts) {
            Post post = saveOrUpdatePost(account, fetchedPost);
            recalculateBuzzScore(post);
            count++;
        }
        return count;
    }

    private Post saveOrUpdatePost(SocialAccount account, FetchedPostData fetchedPost) {
        Optional<Post> existing = postRepository.findByPlatformAndExternalId(account.getPlatform(), fetchedPost.externalId());
        if (existing.isPresent()) {
            Post post = existing.get();
            post.refreshMetrics(fetchedPost);
            return postRepository.save(post);
        }
        Post newPost = Post.fromFetchedData(account.getId(), account.getPlatform(), fetchedPost);
        return postRepository.save(newPost);
    }

    /**
     * AI分析結果を伴わない、公開メトリクスのみに基づくBuzzScore再計算（Strategyパターン）。
     * {@code buzz_scores.post_id}にUNIQUE制約があるため、既存レコードがあればそのIDを引き継いで
     * 更新する（{@link BuzzScore#of}で毎回新規IDを発行すると、2回目以降の同期実行で
     * 制約違反になる）。
     */
    private void recalculateBuzzScore(Post post) {
        BuzzScoreCalculator.CalculationResult result = buzzScoreCalculator.calculate(BuzzScoreInput.withoutAi(post));
        UUID existingId = buzzScoreRepository.findByPostId(post.getId()).map(BuzzScore::getId).orElse(null);
        BuzzScore buzzScore = existingId != null
                ? new BuzzScore(existingId, post.getId(), result.totalScore(), result.breakdown(), OffsetDateTime.now())
                : BuzzScore.of(post.getId(), result.totalScore(), result.breakdown());
        buzzScoreRepository.save(buzzScore);
    }

    /** 急上昇(48時間)/週間(7日)/月間(30日)の各ランキングを、全体版とプラットフォーム別版で再構築する。 */
    private int refreshRankings(int candidatePoolSize, int topN) {
        int total = 0;
        total += refreshRankingForType(RankingType.TRENDING, Duration.ofHours(48), candidatePoolSize, topN);
        total += refreshRankingForType(RankingType.WEEKLY, Duration.ofDays(7), candidatePoolSize, topN);
        total += refreshRankingForType(RankingType.MONTHLY, Duration.ofDays(30), candidatePoolSize, topN);
        return total;
    }

    private int refreshRankingForType(RankingType type, Duration window, int candidatePoolSize, int topN) {
        OffsetDateTime periodEnd = OffsetDateTime.now();
        OffsetDateTime periodStart = periodEnd.minus(window);

        List<ScoredPost> candidates = collectCandidates(periodStart, candidatePoolSize);
        if (candidates.isEmpty()) {
            rankingRepository.deleteByType(type);
            return 0;
        }

        List<Ranking> rankings = new ArrayList<>();
        rankings.addAll(buildRankings(type, null, null, candidates, topN, periodStart, periodEnd));

        Map<Platform, List<ScoredPost>> byPlatform = new EnumMap<>(Platform.class);
        for (ScoredPost scored : candidates) {
            byPlatform.computeIfAbsent(scored.post().getPlatform(), p -> new ArrayList<>()).add(scored);
        }
        for (Platform platform : TRACKED_PLATFORMS) {
            List<ScoredPost> forPlatform = byPlatform.get(platform);
            if (forPlatform != null && !forPlatform.isEmpty()) {
                rankings.addAll(buildRankings(type, null, platform, forPlatform, topN, periodStart, periodEnd));
            }
        }

        // ジャンル別ランキング。AI分析済み（AnalysisResultが存在する）投稿のみが対象で、
        // ジャンルはGenreNormalizerでフロントエンドの英大文字コードに正規化して保存する
        // （そうしないと genre 絞り込み検索が常に空を返してしまう）。
        Map<String, List<ScoredPost>> byGenre = new HashMap<>();
        for (ScoredPost scored : candidates) {
            String genre = genreForPost(scored.post().getId());
            if (genre != null) {
                byGenre.computeIfAbsent(genre, g -> new ArrayList<>()).add(scored);
            }
        }
        for (Map.Entry<String, List<ScoredPost>> entry : byGenre.entrySet()) {
            rankings.addAll(buildRankings(type, entry.getKey(), null, entry.getValue(), topN, periodStart, periodEnd));
        }

        rankingRepository.deleteByType(type);
        rankingRepository.save(rankings);
        return rankings.size();
    }

    private String genreForPost(UUID postId) {
        return analysisResultRepository.findByPostId(postId)
                .map(AnalysisResult::getGenre)
                .map(genreNormalizer::normalize)
                .orElse(null);
    }

    private List<ScoredPost> collectCandidates(OffsetDateTime periodStart, int candidatePoolSize) {
        PostSearchCriteria criteria = new PostSearchCriteria(null, null, null, null,
                0, candidatePoolSize, "publishedAt", false);
        return postRepository.search(criteria).content().stream()
                .filter(post -> post.getPublishedAt() != null && post.getPublishedAt().isAfter(periodStart))
                .map(post -> new ScoredPost(post, buzzScoreRepository.findByPostId(post.getId())
                        .map(BuzzScore::getTotalScore).orElse(0.0)))
                .toList();
    }

    private List<Ranking> buildRankings(RankingType type, String genre, Platform platform,
                                         List<ScoredPost> candidates, int topN,
                                         OffsetDateTime periodStart, OffsetDateTime periodEnd) {
        List<ScoredPost> sorted = candidates.stream()
                .sorted(Comparator.comparingDouble(ScoredPost::score).reversed())
                .limit(topN)
                .toList();
        List<Ranking> rankings = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            ScoredPost scored = sorted.get(i);
            rankings.add(Ranking.createNew(type, genre, platform, scored.post().getId(), i + 1, scored.score(),
                    periodStart, periodEnd));
        }
        return rankings;
    }

    /** バッチ実行後、古い集計結果が返らないよう競合分析/ランキングのRedisキャッシュを全消去する。 */
    private void evictCaches() {
        Cache competitorStatsCache = cacheManager.getCache("competitorStats");
        if (competitorStatsCache != null) {
            competitorStatsCache.clear();
        }
        Cache rankingsCache = cacheManager.getCache("rankings");
        if (rankingsCache != null) {
            rankingsCache.clear();
        }
    }

    private void sleepPolitely(long delayMs) {
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private record ScoredPost(Post post, double score) {
    }
}
