package com.buzzanalysis.application.savedanalysis;

import com.buzzanalysis.application.savedanalysis.dto.SaveAnalysisCommand;
import com.buzzanalysis.application.savedanalysis.dto.SavedAnalysisDetailDto;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysis;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysisRepository;
import com.buzzanalysis.domain.score.BuzzScore;
import com.buzzanalysis.domain.score.BuzzScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link SavedAnalysisApplicationService} の単体テスト。
 * GET /saved-analyses がブックマーク(postIdのみ)しか返さず、フロントエンドが投稿本体・AI分析結果を
 * 参照できずクラッシュするバグが発覚したため、一覧・保存レスポンスが投稿・分析・BuzzScoreを
 * 正しく合成すること、および参照先の投稿が削除済みの場合は一覧から除外されることを検証する。
 */
@ExtendWith(MockitoExtension.class)
class SavedAnalysisApplicationServiceTest {

    @Mock
    private SavedAnalysisRepository savedAnalysisRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private AnalysisResultRepository analysisResultRepository;
    @Mock
    private BuzzScoreRepository buzzScoreRepository;

    private SavedAnalysisApplicationService service;
    private UUID userId;
    private UUID postId;
    private Post existingPost;

    @BeforeEach
    void setUp() {
        service = new SavedAnalysisApplicationService(savedAnalysisRepository, postRepository,
                analysisResultRepository, buzzScoreRepository);

        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
        existingPost = new Post(postId, UUID.randomUUID(), Platform.X, "12345", "https://x.com/user/status/12345",
                OffsetDateTime.now(), "user", "caption", List.of(), 100L, 10L, 1000L, 5L, null, null,
                PostType.TEXT, OffsetDateTime.now(), OffsetDateTime.now());
    }

    @Test
    void list_includesPostAnalysisAndBuzzScore_whenPostWasAnalyzed() {
        SavedAnalysis saved = new SavedAnalysis(UUID.randomUUID(), userId, postId, "memo", OffsetDateTime.now());
        when(savedAnalysisRepository.findByUserId(userId)).thenReturn(List.of(saved));
        when(postRepository.findByIdIn(List.of(postId))).thenReturn(List.of(existingPost));
        when(analysisResultRepository.findByPostIdIn(List.of(postId))).thenReturn(List.of(
                AnalysisResult.builder().id(UUID.randomUUID()).postId(postId).genre("エンタメ").build()));
        when(buzzScoreRepository.findByPostIdIn(List.of(postId))).thenReturn(List.of(
                new BuzzScore(UUID.randomUUID(), postId, 80.0, Map.of("engagementRate", 10.0), OffsetDateTime.now())));

        List<SavedAnalysisDetailDto> result = service.list(userId);

        assertThat(result).hasSize(1);
        SavedAnalysisDetailDto dto = result.get(0);
        assertThat(dto.post().id()).isEqualTo(postId);
        assertThat(dto.analysis()).isNotNull();
        assertThat(dto.buzzScore()).isNotNull();
        assertThat(dto.buzzScore().totalScore()).isEqualTo(80.0);
    }

    @Test
    void list_returnsNullAnalysisAndBuzzScore_whenPostWasNeverAnalyzed() {
        SavedAnalysis saved = new SavedAnalysis(UUID.randomUUID(), userId, postId, null, OffsetDateTime.now());
        when(savedAnalysisRepository.findByUserId(userId)).thenReturn(List.of(saved));
        when(postRepository.findByIdIn(List.of(postId))).thenReturn(List.of(existingPost));
        when(analysisResultRepository.findByPostIdIn(List.of(postId))).thenReturn(List.of());
        when(buzzScoreRepository.findByPostIdIn(List.of(postId))).thenReturn(List.of());

        List<SavedAnalysisDetailDto> result = service.list(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).analysis()).isNull();
        assertThat(result.get(0).buzzScore()).isNull();
    }

    @Test
    void list_excludesEntry_whenReferencedPostNoLongerExists() {
        SavedAnalysis saved = new SavedAnalysis(UUID.randomUUID(), userId, postId, null, OffsetDateTime.now());
        when(savedAnalysisRepository.findByUserId(userId)).thenReturn(List.of(saved));
        when(postRepository.findByIdIn(List.of(postId))).thenReturn(List.of());
        when(analysisResultRepository.findByPostIdIn(List.of(postId))).thenReturn(List.of());
        when(buzzScoreRepository.findByPostIdIn(List.of(postId))).thenReturn(List.of());

        List<SavedAnalysisDetailDto> result = service.list(userId);

        assertThat(result).isEmpty();
    }

    @Test
    void list_neverCallsPerItemLookups_evenWithMultipleSavedEntries() {
        UUID postId2 = UUID.randomUUID();
        Post post2 = new Post(postId2, UUID.randomUUID(), Platform.X, "67890", "https://x.com/user/status/67890",
                OffsetDateTime.now(), "user2", "caption2", List.of(), 50L, 5L, 500L, 2L, null, null,
                PostType.TEXT, OffsetDateTime.now(), OffsetDateTime.now());
        SavedAnalysis saved1 = new SavedAnalysis(UUID.randomUUID(), userId, postId, null, OffsetDateTime.now());
        SavedAnalysis saved2 = new SavedAnalysis(UUID.randomUUID(), userId, postId2, null, OffsetDateTime.now());
        when(savedAnalysisRepository.findByUserId(userId)).thenReturn(List.of(saved1, saved2));
        when(postRepository.findByIdIn(any())).thenReturn(List.of(existingPost, post2));
        when(analysisResultRepository.findByPostIdIn(any())).thenReturn(List.of());
        when(buzzScoreRepository.findByPostIdIn(any())).thenReturn(List.of());

        List<SavedAnalysisDetailDto> result = service.list(userId);

        assertThat(result).hasSize(2);
        org.mockito.Mockito.verify(postRepository, org.mockito.Mockito.never()).findById(any());
        org.mockito.Mockito.verify(analysisResultRepository, org.mockito.Mockito.never()).findByPostId(any());
        org.mockito.Mockito.verify(buzzScoreRepository, org.mockito.Mockito.never()).findByPostId(any());
    }

    @Test
    void save_persistsBookmarkAndReturnsEnrichedDetail() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));
        when(savedAnalysisRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(analysisResultRepository.findByPostId(postId)).thenReturn(Optional.empty());
        when(buzzScoreRepository.findByPostId(postId)).thenReturn(Optional.empty());

        SavedAnalysisDetailDto result = service.save(new SaveAnalysisCommand(userId, postId, "memo"));

        assertThat(result.post().id()).isEqualTo(postId);
        assertThat(result.note()).isEqualTo("memo");
    }
}
