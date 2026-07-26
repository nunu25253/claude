package com.buzzanalysis.application.analytics;

import com.buzzanalysis.application.analytics.dto.AiImprovementRateDto;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** {@link AiImprovementRateApplicationService} の単体テスト。 */
@ExtendWith(MockitoExtension.class)
class AiImprovementRateApplicationServiceTest {

    @Mock
    private BuzzScoreHistoryRepository buzzScoreHistoryRepository;

    private AiImprovementRateApplicationService service;

    @BeforeEach
    void setUp() {
        service = new AiImprovementRateApplicationService(buzzScoreHistoryRepository);
    }

    @Test
    void compute_returnsNulls_whenNoPostsHaveBeenReanalyzed() {
        when(buzzScoreHistoryRepository.findPostIdsWithAtLeastTwoEntries(500)).thenReturn(List.of());

        AiImprovementRateDto result = service.compute();

        assertThat(result.sampleSize()).isZero();
        assertThat(result.improvedPercentage()).isNull();
        assertThat(result.averageScoreDelta()).isNull();
    }

    @Test
    void compute_computesImprovedPercentageAndAverageDelta_acrossReanalyzedPosts() {
        UUID improvedPost = UUID.randomUUID();
        UUID worsenedPost = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        when(buzzScoreHistoryRepository.findPostIdsWithAtLeastTwoEntries(500))
                .thenReturn(List.of(improvedPost, worsenedPost));
        when(buzzScoreHistoryRepository.findByPostIdOrderByCalculatedAtAsc(improvedPost)).thenReturn(List.of(
                new BuzzScoreHistoryEntry(UUID.randomUUID(), improvedPost, 60.0, Map.of(), now.minusDays(2)),
                new BuzzScoreHistoryEntry(UUID.randomUUID(), improvedPost, 80.0, Map.of(), now)));
        when(buzzScoreHistoryRepository.findByPostIdOrderByCalculatedAtAsc(worsenedPost)).thenReturn(List.of(
                new BuzzScoreHistoryEntry(UUID.randomUUID(), worsenedPost, 70.0, Map.of(), now.minusDays(2)),
                new BuzzScoreHistoryEntry(UUID.randomUUID(), worsenedPost, 65.0, Map.of(), now)));

        AiImprovementRateDto result = service.compute();

        assertThat(result.sampleSize()).isEqualTo(2);
        assertThat(result.improvedPercentage()).isEqualTo(50.0);
        assertThat(result.averageScoreDelta()).isEqualTo(7.5); // (+20 + -5) / 2
    }

    @Test
    void compute_usesFirstAndLatestEntry_whenMoreThanTwoReanalysesExist() {
        UUID postId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        when(buzzScoreHistoryRepository.findPostIdsWithAtLeastTwoEntries(500)).thenReturn(List.of(postId));
        when(buzzScoreHistoryRepository.findByPostIdOrderByCalculatedAtAsc(postId)).thenReturn(List.of(
                new BuzzScoreHistoryEntry(UUID.randomUUID(), postId, 50.0, Map.of(), now.minusDays(3)),
                new BuzzScoreHistoryEntry(UUID.randomUUID(), postId, 40.0, Map.of(), now.minusDays(2)),
                new BuzzScoreHistoryEntry(UUID.randomUUID(), postId, 90.0, Map.of(), now)));

        AiImprovementRateDto result = service.compute();

        assertThat(result.sampleSize()).isEqualTo(1);
        assertThat(result.improvedPercentage()).isEqualTo(100.0);
        assertThat(result.averageScoreDelta()).isEqualTo(40.0); // 90 - 50
    }
}
