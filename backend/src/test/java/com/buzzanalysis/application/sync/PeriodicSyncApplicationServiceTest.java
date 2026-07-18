package com.buzzanalysis.application.sync;

import com.buzzanalysis.application.sync.dto.SyncSummaryDto;
import com.buzzanalysis.domain.account.SocialAccount;
import com.buzzanalysis.domain.account.SocialAccountRepository;
import com.buzzanalysis.domain.platform.FetchedPostData;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.platform.PlatformFactory;
import com.buzzanalysis.domain.platform.SocialPlatform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostSearchResult;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.ranking.Ranking;
import com.buzzanalysis.domain.ranking.RankingRepository;
import com.buzzanalysis.domain.ranking.RankingType;
import com.buzzanalysis.domain.score.BuzzScore;
import com.buzzanalysis.domain.score.BuzzScoreCalculator;
import com.buzzanalysis.domain.score.BuzzScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PeriodicSyncApplicationService}（定期データ取得バッチ）のMockitoによる単体テスト。
 * 外部SNS APIクライアント（{@link SocialPlatform}）・リポジトリ類はすべてモック化する。
 */
@ExtendWith(MockitoExtension.class)
class PeriodicSyncApplicationServiceTest {

    @Mock
    private SocialAccountRepository socialAccountRepository;
    @Mock
    private PlatformFactory platformFactory;
    @Mock
    private PostRepository postRepository;
    @Mock
    private BuzzScoreCalculator buzzScoreCalculator;
    @Mock
    private BuzzScoreRepository buzzScoreRepository;
    @Mock
    private RankingRepository rankingRepository;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private SocialPlatform instagramClient;

    private PeriodicSyncApplicationService service;

    @BeforeEach
    void setUp() {
        service = new PeriodicSyncApplicationService(socialAccountRepository, platformFactory, postRepository,
                buzzScoreCalculator, buzzScoreRepository, rankingRepository, cacheManager);
        when(cacheManager.getCache(any())).thenReturn(null);
        when(postRepository.search(any())).thenReturn(new PostSearchResult(List.of(), 0, 0, 0));
    }

    @Test
    void syncAll_upsertsPostsAndRecalculatesBuzzScoreForTrackedAccounts() {
        SocialAccount account = SocialAccount.createNew(Platform.INSTAGRAM, "ext-1", "demo_creator",
                "Demo Creator", "https://www.instagram.com/demo_creator/", 10_000L, 100L);
        when(socialAccountRepository.findAllTrackingEnabled()).thenReturn(List.of(account));
        when(platformFactory.resolve(Platform.INSTAGRAM)).thenReturn(instagramClient);

        FetchedPostData fetched = new FetchedPostData("ig-post-1", "https://www.instagram.com/reel/ig-post-1/",
                OffsetDateTime.now().minusHours(2), "demo_creator", "caption", List.of("tag"),
                1000L, 50L, 20000L, 30L, 15, null, PostType.REEL);
        when(instagramClient.fetchRecentPosts("demo_creator", 20)).thenReturn(List.of(fetched));
        when(postRepository.findByPlatformAndExternalId(Platform.INSTAGRAM, "ig-post-1")).thenReturn(Optional.empty());
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(buzzScoreCalculator.calculate(any())).thenReturn(new BuzzScoreCalculator.CalculationResult(72.5, Map.of()));

        SyncSummaryDto summary = service.syncAll(20, 0, 200, 20);

        assertThat(summary.trackedAccountCount()).isEqualTo(1);
        assertThat(summary.syncedPostCount()).isEqualTo(1);
        assertThat(summary.accountErrors()).isEmpty();

        ArgumentCaptor<BuzzScore> scoreCaptor = ArgumentCaptor.forClass(BuzzScore.class);
        verify(buzzScoreRepository).save(scoreCaptor.capture());
        assertThat(scoreCaptor.getValue().getTotalScore()).isEqualTo(72.5);
    }

    @Test
    void syncAll_recordsErrorAndContinues_whenAccountSyncFails() {
        SocialAccount broken = SocialAccount.createNew(Platform.TIKTOK, "ext-2", "broken_account",
                "Broken", null, null, null);
        when(socialAccountRepository.findAllTrackingEnabled()).thenReturn(List.of(broken));
        when(platformFactory.resolve(Platform.TIKTOK)).thenThrow(new IllegalStateException("platform unavailable"));

        SyncSummaryDto summary = service.syncAll(20, 0, 200, 20);

        assertThat(summary.trackedAccountCount()).isEqualTo(1);
        assertThat(summary.syncedPostCount()).isEqualTo(0);
        assertThat(summary.accountErrors()).hasSize(1);
        assertThat(summary.accountErrors().get(0)).contains("broken_account");
    }

    @Test
    void syncAll_refreshesRankings_keepingTopNSortedByScoreDescending() {
        when(socialAccountRepository.findAllTrackingEnabled()).thenReturn(List.of());

        Post lowScore = samplePost("low", Platform.INSTAGRAM, OffsetDateTime.now().minusHours(1));
        Post highScore = samplePost("high", Platform.INSTAGRAM, OffsetDateTime.now().minusHours(2));
        when(postRepository.search(any())).thenReturn(new PostSearchResult(List.of(lowScore, highScore), 0, 200, 2));
        when(buzzScoreRepository.findByPostId(lowScore.getId())).thenReturn(Optional.of(BuzzScore.of(lowScore.getId(), 10.0, Map.of())));
        when(buzzScoreRepository.findByPostId(highScore.getId())).thenReturn(Optional.of(BuzzScore.of(highScore.getId(), 90.0, Map.of())));

        service.syncAll(20, 0, 200, 20);

        // TRENDING/WEEKLY/MONTHLYの3種別、各「全体版」+「INSTAGRAM別」の2グループでdeleteByTypeが呼ばれる
        verify(rankingRepository, times(3)).deleteByType(any(RankingType.class));

        ArgumentCaptor<List<Ranking>> savedCaptor = ArgumentCaptor.forClass(List.class);
        verify(rankingRepository, times(3)).save(savedCaptor.capture());

        List<Ranking> trendingRankings = savedCaptor.getAllValues().stream()
                .flatMap(List::stream)
                .filter(r -> r.getType() == RankingType.TRENDING && r.getPlatform() == null)
                .sorted((a, b) -> Integer.compare(a.getRankPosition(), b.getRankPosition()))
                .toList();

        assertThat(trendingRankings).hasSize(2);
        assertThat(trendingRankings.get(0).getPostId()).isEqualTo(highScore.getId());
        assertThat(trendingRankings.get(0).getRankPosition()).isEqualTo(1);
        assertThat(trendingRankings.get(1).getPostId()).isEqualTo(lowScore.getId());
    }

    private Post samplePost(String externalId, Platform platform, OffsetDateTime publishedAt) {
        return new Post(UUID.randomUUID(), UUID.randomUUID(), platform, externalId,
                "https://example.com/" + externalId, publishedAt, "creator", "caption", List.of("tag"),
                1000L, 50L, 20000L, 30L, 15, null, PostType.REEL, OffsetDateTime.now(), OffsetDateTime.now());
    }
}
