package com.buzzanalysis.application.event;

import com.buzzanalysis.application.rag.RagIndexingApplicationService;
import com.buzzanalysis.application.rag.dto.RagIndexRequest;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.rag.RagSourceType;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysisCreatedEvent;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SavedAnalysisRagIndexingListenerTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private AnalysisResultRepository analysisResultRepository;
    @Mock
    private RagIndexingApplicationService ragIndexingApplicationService;

    private SavedAnalysisRagIndexingListener listener;
    private UUID userId;
    private UUID postId;

    @BeforeEach
    void setUp() {
        listener = new SavedAnalysisRagIndexingListener(postRepository, analysisResultRepository, ragIndexingApplicationService);
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
    }

    @Test
    void onSavedAnalysisCreated_indexesAnalysisResult_intoRagForTheOwningUser() {
        UUID analysisResultId = UUID.randomUUID();
        AnalysisResult analysisResult = AnalysisResult.builder()
                .id(analysisResultId).postId(postId).genre("美容").subGenre("スキンケア")
                .whyItWentViral("共感を呼ぶビフォーアフター").targetAudience("20代女性")
                .hook("冒頭のフック").callToAction("プロフィールへ誘導")
                .postStructureAnalysis("起承転結").strengths("強み").weaknesses("弱み")
                .improvementSuggestions("改善提案").build();
        when(analysisResultRepository.findByPostId(postId)).thenReturn(Optional.of(analysisResult));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post()));

        listener.onSavedAnalysisCreated(new SavedAnalysisCreatedEvent(UUID.randomUUID(), userId, postId));

        ArgumentCaptor<RagIndexRequest> captor = ArgumentCaptor.forClass(RagIndexRequest.class);
        verify(ragIndexingApplicationService).index(captor.capture(), eq(userId));
        assertThat(captor.getValue().sourceType()).isEqualTo(RagSourceType.ANALYSIS_RESULT);
        assertThat(captor.getValue().sourceId()).isEqualTo(analysisResultId);
        assertThat(captor.getValue().contentText()).contains("共感を呼ぶビフォーアフター", "20代女性", "冒頭のフック");
    }

    @Test
    void onSavedAnalysisCreated_doesNothing_whenPostHasNoAnalysisResultYet() {
        when(analysisResultRepository.findByPostId(postId)).thenReturn(Optional.empty());

        listener.onSavedAnalysisCreated(new SavedAnalysisCreatedEvent(UUID.randomUUID(), userId, postId));

        verifyNoInteractions(ragIndexingApplicationService);
        verifyNoInteractions(postRepository);
    }

    @Test
    void onSavedAnalysisCreated_doesNothing_whenPostWasDeleted() {
        AnalysisResult analysisResult = AnalysisResult.builder().id(UUID.randomUUID()).postId(postId).build();
        when(analysisResultRepository.findByPostId(postId)).thenReturn(Optional.of(analysisResult));
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        listener.onSavedAnalysisCreated(new SavedAnalysisCreatedEvent(UUID.randomUUID(), userId, postId));

        verifyNoInteractions(ragIndexingApplicationService);
    }

    private Post post() {
        return new Post(postId, UUID.randomUUID(), Platform.INSTAGRAM, "ext-1", "https://example.com/1",
                OffsetDateTime.now(), "creator", "投稿キャプション", List.of(), 10L, 2L, 100L, null, null, null,
                PostType.IMAGE, OffsetDateTime.now(), OffsetDateTime.now());
    }
}
