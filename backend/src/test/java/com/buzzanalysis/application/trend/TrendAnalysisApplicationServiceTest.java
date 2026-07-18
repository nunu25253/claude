package com.buzzanalysis.application.trend;

import com.buzzanalysis.application.trend.dto.TrendAnalysisRequest;
import com.buzzanalysis.application.trend.dto.TrendReportDto;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.normalization.DefaultPostNormalizer;
import com.buzzanalysis.domain.normalization.PostFieldMapper;
import com.buzzanalysis.domain.normalization.strategy.DefaultNormalizationStrategy;
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
import com.buzzanalysis.domain.trend.TrendCategory;
import com.buzzanalysis.domain.trend.TrendReport;
import com.buzzanalysis.domain.trend.TrendReportRepository;
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

@ExtendWith(MockitoExtension.class)
class TrendAnalysisApplicationServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private AnalysisResultRepository analysisResultRepository;
    @Mock
    private AiTrendSummaryPort aiTrendSummaryPort;
    @Mock
    private TrendReportRepository trendReportRepository;

    private TrendAnalysisApplicationService service;

    @BeforeEach
    void setUp() {
        DefaultPostNormalizer normalizer = new DefaultPostNormalizer(new PostFieldMapper(),
                List.of(new DefaultNormalizationStrategy()));
        DefaultPostPreprocessor preprocessor = new DefaultPostPreprocessor(
                new DefaultTextCleaner(List.of()), new HeuristicLanguageDetector(), new HashtagExtractor(),
                new MentionExtractor(), new PostingTimeAnalyzer(), new VideoDurationAnalyzer(),
                new ContentFormatClassifier());
        service = new TrendAnalysisApplicationService(postRepository, analysisResultRepository, normalizer,
                preprocessor, aiTrendSummaryPort, trendReportRepository);
        when(analysisResultRepository.findByPostId(any())).thenReturn(Optional.empty());
        when(aiTrendSummaryPort.summarize(any())).thenReturn("サマリー");
        when(trendReportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void analyze_detectsGrowingHashtag_withPositiveGrowthRate() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Post> posts = List.of(
                post(now.minusDays(1), "#美容 の話"), post(now.minusDays(2), "#美容 の話"),
                post(now.minusDays(3), "#美容 の話"),
                post(now.minusDays(15), "#美容 の話")
        );
        when(postRepository.search(any())).thenReturn(new PostSearchResult(posts, 0, 500, posts.size()));

        TrendReportDto result = service.analyze(new TrendAnalysisRequest(null, 7, 21));

        var hashtagItems = result.items().stream().filter(i -> i.category() == TrendCategory.HASHTAG).toList();
        assertThat(hashtagItems).hasSize(1);
        assertThat(hashtagItems.get(0).value()).isEqualTo("美容");
        assertThat(hashtagItems.get(0).recentCount()).isEqualTo(3);
        assertThat(hashtagItems.get(0).baselineCount()).isEqualTo(1);
        assertThat(hashtagItems.get(0).growthRatePercent()).isEqualTo(200.0);
        assertThat(hashtagItems.get(0).emerging()).isFalse();
    }

    @Test
    void analyze_marksEmerging_whenBaselineCountIsZero() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Post> posts = List.of(
                post(now.minusDays(1), "#新企画 開始"), post(now.minusDays(2), "#新企画 開始"),
                post(now.minusDays(3), "#新企画 開始")
        );
        when(postRepository.search(any())).thenReturn(new PostSearchResult(posts, 0, 500, posts.size()));

        TrendReportDto result = service.analyze(new TrendAnalysisRequest(null, 7, 21));

        var hashtagItems = result.items().stream().filter(i -> i.category() == TrendCategory.HASHTAG).toList();
        assertThat(hashtagItems).hasSize(1);
        assertThat(hashtagItems.get(0).emerging()).isTrue();
        assertThat(hashtagItems.get(0).growthRatePercent()).isNull();
        assertThat(hashtagItems.get(0).baselineCount()).isEqualTo(0);
    }

    @Test
    void analyze_excludesItemsBelowMinimumSampleSize() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Post> posts = List.of(post(now.minusDays(1), "#レア 投稿"), post(now.minusDays(2), "#レア 投稿"));
        when(postRepository.search(any())).thenReturn(new PostSearchResult(posts, 0, 500, posts.size()));

        TrendReportDto result = service.analyze(new TrendAnalysisRequest(null, 7, 21));

        assertThat(result.items()).noneMatch(i -> "レア".equals(i.value()));
    }

    @Test
    void analyze_excludesPostsOutsideBaselineWindow() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Post> posts = List.of(
                post(now.minusDays(1), "#古い 話"), post(now.minusDays(2), "#古い 話"), post(now.minusDays(3), "#古い 話"),
                post(now.minusDays(60), "#古い 話"), post(now.minusDays(61), "#古い 話"), post(now.minusDays(62), "#古い 話")
        );
        when(postRepository.search(any())).thenReturn(new PostSearchResult(posts, 0, 500, posts.size()));

        TrendReportDto result = service.analyze(new TrendAnalysisRequest(null, 7, 21));

        var item = result.items().stream().filter(i -> "古い".equals(i.value())).findFirst().orElseThrow();
        assertThat(item.baselineCount()).isEqualTo(0);
        assertThat(item.emerging()).isTrue();
    }

    private Post post(OffsetDateTime publishedAt, String caption) {
        return new Post(UUID.randomUUID(), UUID.randomUUID(), Platform.INSTAGRAM, "ext-" + UUID.randomUUID(),
                "https://instagram.com/p/x", publishedAt, "creator", caption, List.of(),
                100L, 10L, null, null, null, 1, PostType.CAROUSEL, OffsetDateTime.now(), OffsetDateTime.now());
    }
}
