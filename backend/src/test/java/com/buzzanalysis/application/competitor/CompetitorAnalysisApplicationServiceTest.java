package com.buzzanalysis.application.competitor;

import com.buzzanalysis.application.competitor.dto.CompetitorStatsDto;
import com.buzzanalysis.domain.account.SocialAccount;
import com.buzzanalysis.domain.account.SocialAccountRepository;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.competitor.CompetitorStats;
import com.buzzanalysis.domain.competitor.CompetitorStatsRepository;
import com.buzzanalysis.domain.normalization.DefaultPostNormalizer;
import com.buzzanalysis.domain.normalization.PostFieldMapper;
import com.buzzanalysis.domain.normalization.strategy.DefaultNormalizationStrategy;
import com.buzzanalysis.domain.normalization.strategy.InstagramNormalizationStrategy;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostSearchResult;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.preprocessing.ContentFormatClassifier;
import com.buzzanalysis.domain.preprocessing.DefaultPostPreprocessor;
import com.buzzanalysis.domain.preprocessing.DefaultTextCleaner;
import com.buzzanalysis.domain.preprocessing.HashtagExtractor;
import com.buzzanalysis.domain.preprocessing.HeuristicLanguageDetector;
import com.buzzanalysis.domain.preprocessing.MentionExtractor;
import com.buzzanalysis.domain.preprocessing.PostingTimeAnalyzer;
import com.buzzanalysis.domain.preprocessing.VideoDurationAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link CompetitorAnalysisApplicationService} の単体テスト（Phase9で新規追加された
 * 平均再生数・投稿形式分布・ジャンル分布を中心に検証する）。PostNormalizer/PostPreprocessorは
 * 実オブジェクト（Phase1/2のPOJO実装）を使用する。
 */
@ExtendWith(MockitoExtension.class)
class CompetitorAnalysisApplicationServiceTest {

    @Mock
    private SocialAccountRepository socialAccountRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private CompetitorStatsRepository competitorStatsRepository;
    @Mock
    private AnalysisResultRepository analysisResultRepository;

    private CompetitorAnalysisApplicationService service;

    @BeforeEach
    void setUp() {
        DefaultPostNormalizer normalizer = new DefaultPostNormalizer(new PostFieldMapper(),
                List.of(new InstagramNormalizationStrategy(), new DefaultNormalizationStrategy()));
        DefaultPostPreprocessor preprocessor = new DefaultPostPreprocessor(
                new DefaultTextCleaner(List.of()), new HeuristicLanguageDetector(), new HashtagExtractor(),
                new MentionExtractor(), new PostingTimeAnalyzer(), new VideoDurationAnalyzer(),
                new ContentFormatClassifier());
        service = new CompetitorAnalysisApplicationService(socialAccountRepository, postRepository,
                competitorStatsRepository, analysisResultRepository, normalizer, preprocessor);
        when(competitorStatsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void getStats_computesAverageViewCount_excludingUnmeasuredPosts() {
        UUID accountId = UUID.randomUUID();
        when(socialAccountRepository.findById(accountId)).thenReturn(Optional.of(sampleAccount(accountId)));

        // Instagram CAROUSELは再生数が未計測扱い(Phase1のStrategyにより除外される)、REELは実測値として使われる
        Post carousel = post(accountId, PostType.CAROUSEL, 5000L, 2);
        Post reel = post(accountId, PostType.REEL, 8000L, null);
        when(postRepository.search(any())).thenReturn(new PostSearchResult(List.of(carousel, reel), 0, 500, 2));
        when(analysisResultRepository.findByPostId(any())).thenReturn(Optional.empty());

        CompetitorStatsDto result = service.getStats(accountId);

        assertThat(result.averageViewCount()).isEqualTo(8000.0);
    }

    @Test
    void getStats_computesGenreDistribution_fromAnalysisResultsOnly() {
        UUID accountId = UUID.randomUUID();
        when(socialAccountRepository.findById(accountId)).thenReturn(Optional.of(sampleAccount(accountId)));

        Post analyzed = post(accountId, PostType.REEL, 1000L, null);
        Post unanalyzed = post(accountId, PostType.REEL, 1000L, null);
        when(postRepository.search(any())).thenReturn(new PostSearchResult(List.of(analyzed, unanalyzed), 0, 500, 2));
        when(analysisResultRepository.findByPostId(analyzed.getId())).thenReturn(
                Optional.of(AnalysisResult.builder().postId(analyzed.getId()).genre("美容").build()));
        when(analysisResultRepository.findByPostId(unanalyzed.getId())).thenReturn(Optional.empty());

        CompetitorStatsDto result = service.getStats(accountId);

        assertThat(result.genreDistribution()).containsEntry("美容", 100.0);
    }

    @Test
    void getStats_returnsEmptyStats_whenNoPosts() {
        UUID accountId = UUID.randomUUID();
        when(socialAccountRepository.findById(accountId)).thenReturn(Optional.of(sampleAccount(accountId)));
        when(postRepository.search(any())).thenReturn(new PostSearchResult(List.of(), 0, 500, 0));

        CompetitorStatsDto result = service.getStats(accountId);

        assertThat(result.averageViewCount()).isNull();
        assertThat(result.genreDistribution()).isEmpty();
    }

    private SocialAccount sampleAccount(UUID id) {
        return new SocialAccount(id, Platform.INSTAGRAM, "ext", "user", "User", null, 1000L, 50L, true,
                OffsetDateTime.now(), OffsetDateTime.now());
    }

    private Post post(UUID accountId, PostType postType, long viewCount, Integer imageCount) {
        return new Post(UUID.randomUUID(), accountId, Platform.INSTAGRAM, "ext-" + UUID.randomUUID(),
                "https://instagram.com/p/x", OffsetDateTime.now(), "creator", "caption", List.of(),
                100L, 10L, viewCount, null, postType == PostType.REEL ? 30 : null, imageCount, postType,
                OffsetDateTime.now(), OffsetDateTime.now());
    }
}
