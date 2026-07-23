package com.buzzanalysis.application.post;

import com.buzzanalysis.application.post.dto.BuzzScoreHistoryPointDto;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.score.BuzzScoreHistoryEntry;
import com.buzzanalysis.domain.score.BuzzScoreHistoryRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/** {@link BuzzScoreHistoryApplicationService} の単体テスト。 */
@ExtendWith(MockitoExtension.class)
class BuzzScoreHistoryApplicationServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private BuzzScoreHistoryRepository buzzScoreHistoryRepository;

    private BuzzScoreHistoryApplicationService service;
    private UUID postId;

    @BeforeEach
    void setUp() {
        service = new BuzzScoreHistoryApplicationService(postRepository, buzzScoreHistoryRepository);
        postId = UUID.randomUUID();
    }

    @Test
    void getHistory_returnsPointsInAscendingOrder_whenPostExists() {
        Post post = new Post(postId, UUID.randomUUID(), Platform.X, "12345", "https://x.com/user/status/12345",
                OffsetDateTime.now(), "user", "caption", List.of(), 100L, 10L, 1000L, 5L, null, null,
                PostType.TEXT, OffsetDateTime.now(), OffsetDateTime.now());
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        OffsetDateTime first = OffsetDateTime.now().minusDays(2);
        OffsetDateTime second = OffsetDateTime.now();
        when(buzzScoreHistoryRepository.findByPostIdOrderByCalculatedAtAsc(postId)).thenReturn(List.of(
                BuzzScoreHistoryEntry.of(postId, 40.0, Map.of(), first),
                BuzzScoreHistoryEntry.of(postId, 65.0, Map.of(), second)
        ));

        List<BuzzScoreHistoryPointDto> result = service.getHistory(postId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).totalScore()).isEqualTo(40.0);
        assertThat(result.get(1).totalScore()).isEqualTo(65.0);
    }

    @Test
    void getHistory_throwsEntityNotFound_whenPostDoesNotExist() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getHistory(postId)).isInstanceOf(EntityNotFoundException.class);
    }
}
