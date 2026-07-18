package com.buzzanalysis.application.commonality;

import com.buzzanalysis.application.commonality.dto.CommonalityAnalysisResultDto;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.commonality.CommonalityStatisticsCalculator;
import com.buzzanalysis.domain.normalization.NormalizedPost;
import com.buzzanalysis.domain.normalization.PostNormalizer;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.preprocessing.PostPreprocessor;
import com.buzzanalysis.domain.preprocessing.PreprocessedPost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommonalityAnalysisApplicationServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private AnalysisResultRepository analysisResultRepository;
    @Mock
    private PostNormalizer postNormalizer;
    @Mock
    private PostPreprocessor postPreprocessor;
    @Mock
    private CommonalityStatisticsCalculator statisticsCalculator;
    @Mock
    private AiCommonalityAnalysisPort aiCommonalityAnalysisPort;

    private CommonalityAnalysisApplicationService service;

    @BeforeEach
    void setUp() {
        service = new CommonalityAnalysisApplicationService(postRepository, analysisResultRepository, postNormalizer,
                postPreprocessor, statisticsCalculator, aiCommonalityAnalysisPort);
    }

    @Test
    void analyze_buildsSnippetsOnlyForPostsWithAnalysisResult() {
        UUID analyzedId = UUID.randomUUID();
        UUID unanalyzedId = UUID.randomUUID();
        Post analyzedPost = post(analyzedId);
        Post unanalyzedPost = post(unanalyzedId);
        when(postRepository.findById(analyzedId)).thenReturn(Optional.of(analyzedPost));
        when(postRepository.findById(unanalyzedId)).thenReturn(Optional.of(unanalyzedPost));
        when(postNormalizer.normalize(any())).thenReturn(NormalizedPost.builder().postId(UUID.randomUUID()).build());
        when(postPreprocessor.preprocess(any())).thenReturn(PreprocessedPost.builder().postId(UUID.randomUUID()).build());
        when(analysisResultRepository.findByPostId(analyzedId)).thenReturn(Optional.of(
                AnalysisResult.builder().postId(analyzedId).titleAnalysis("タイトル").hook("フック")
                        .callToAction("CTA").postStructureAnalysis("構成").targetAudience("ターゲット").build()));
        when(analysisResultRepository.findByPostId(unanalyzedId)).thenReturn(Optional.empty());
        when(statisticsCalculator.topHashtags(any())).thenReturn(List.of());
        when(statisticsCalculator.medianVideoDurationSeconds(any())).thenReturn(null);
        when(statisticsCalculator.mostCommonPostingHour(any())).thenReturn(null);
        when(statisticsCalculator.mostCommonContentFormat(any())).thenReturn(null);
        when(aiCommonalityAnalysisPort.analyze(any())).thenReturn(
                new AiCommonalityAnalysisPort.AiCommonalityOutput("t", "h", "c", "s", "ta"));

        CommonalityAnalysisResultDto result = service.analyze(List.of(analyzedId, unanalyzedId));

        assertThat(result.totalPostCount()).isEqualTo(2);
        assertThat(result.aiSampleSize()).isEqualTo(1);

        ArgumentCaptor<List<AiCommonalityAnalysisPort.PostSnippet>> captor = ArgumentCaptor.forClass(List.class);
        verify(aiCommonalityAnalysisPort).analyze(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).titleSnippet()).isEqualTo("タイトル");
    }

    @Test
    void analyze_truncatesLongSnippetsTo80Characters() {
        UUID postId = UUID.randomUUID();
        Post post = post(postId);
        String longText = "あ".repeat(200);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postNormalizer.normalize(any())).thenReturn(NormalizedPost.builder().postId(UUID.randomUUID()).build());
        when(postPreprocessor.preprocess(any())).thenReturn(PreprocessedPost.builder().postId(UUID.randomUUID()).build());
        when(analysisResultRepository.findByPostId(postId)).thenReturn(Optional.of(
                AnalysisResult.builder().postId(postId).titleAnalysis(longText).build()));
        when(statisticsCalculator.topHashtags(any())).thenReturn(List.of());
        when(statisticsCalculator.medianVideoDurationSeconds(any())).thenReturn(null);
        when(statisticsCalculator.mostCommonPostingHour(any())).thenReturn(null);
        when(statisticsCalculator.mostCommonContentFormat(any())).thenReturn(null);
        when(aiCommonalityAnalysisPort.analyze(any())).thenReturn(
                new AiCommonalityAnalysisPort.AiCommonalityOutput("t", "h", "c", "s", "ta"));

        service.analyze(List.of(postId));

        ArgumentCaptor<List<AiCommonalityAnalysisPort.PostSnippet>> captor = ArgumentCaptor.forClass(List.class);
        verify(aiCommonalityAnalysisPort).analyze(captor.capture());
        assertThat(captor.getValue().get(0).titleSnippet()).hasSize(83); // 80文字 + "..."
    }

    private Post post(UUID id) {
        return new Post(id, UUID.randomUUID(), Platform.INSTAGRAM, "ext-" + id, "https://example.com/" + id,
                OffsetDateTime.now(), "creator", "caption", List.of(), 10L, 2L, 100L, null, null, null,
                PostType.IMAGE, OffsetDateTime.now(), OffsetDateTime.now());
    }
}
