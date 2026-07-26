package com.buzzanalysis.application.savedanalysis;

import com.buzzanalysis.application.auth.MailSenderPort;
import com.buzzanalysis.application.auth.dto.GenreScoreSummary;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.genre.GenreNormalizer;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostSearchResult;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysis;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysisRepository;
import com.buzzanalysis.domain.score.BuzzScore;
import com.buzzanalysis.domain.score.BuzzScoreHistoryEntry;
import com.buzzanalysis.domain.score.BuzzScoreHistoryRepository;
import com.buzzanalysis.domain.score.BuzzScoreRepository;
import com.buzzanalysis.domain.user.Role;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** {@link WeeklyDigestApplicationService} の単体テスト。 */
@ExtendWith(MockitoExtension.class)
class WeeklyDigestApplicationServiceTest {

    @Mock
    private SavedAnalysisRepository savedAnalysisRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private AnalysisResultRepository analysisResultRepository;
    @Mock
    private BuzzScoreRepository buzzScoreRepository;
    @Mock
    private BuzzScoreHistoryRepository buzzScoreHistoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MailSenderPort mailSenderPort;

    private WeeklyDigestApplicationService service;
    private UUID userId;
    private UUID postId;
    private User user;

    @BeforeEach
    void setUp() {
        service = new WeeklyDigestApplicationService(savedAnalysisRepository, postRepository,
                analysisResultRepository, buzzScoreRepository, buzzScoreHistoryRepository, userRepository,
                new GenreNormalizer(), mailSenderPort);

        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
        user = new User(userId, "user@example.com", "hash", "User", Role.USER, true,
                OffsetDateTime.now(), OffsetDateTime.now());

        lenient().when(postRepository.search(any())).thenReturn(new PostSearchResult(List.of(), 0, 500, 0));
        lenient().when(analysisResultRepository.findByPostIdIn(any())).thenReturn(List.of());
        lenient().when(buzzScoreRepository.findByPostIdIn(any())).thenReturn(List.of());
    }

    @Test
    void sendDigests_sendsDigest_whenUserHadScoreActivityThisWeek() {
        SavedAnalysis saved = new SavedAnalysis(UUID.randomUUID(), userId, postId, null, OffsetDateTime.now(), null, null);
        when(savedAnalysisRepository.findDistinctUserIds()).thenReturn(List.of(userId));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(savedAnalysisRepository.findByUserId(userId)).thenReturn(List.of(saved));

        OffsetDateTime now = OffsetDateTime.now();
        when(buzzScoreHistoryRepository.findByPostIdOrderByCalculatedAtAsc(postId)).thenReturn(List.of(
                BuzzScoreHistoryEntry.of(postId, 60.0, Map.of(), now.minusDays(20)),
                BuzzScoreHistoryEntry.of(postId, 70.0, Map.of(), now.minusDays(2))));

        int sentCount = service.sendDigests();

        assertThat(sentCount).isEqualTo(1);
        ArgumentCaptor<Double> lastWeekCaptor = ArgumentCaptor.forClass(Double.class);
        verify(mailSenderPort).sendWeeklyDigestEmail(eq(user.getEmail()), any(), eq(70.0),
                lastWeekCaptor.capture(), eq(1));
        assertThat(lastWeekCaptor.getValue()).isNull();
    }

    @Test
    void sendDigests_skipsUser_whenNoScoreActivityThisWeek() {
        SavedAnalysis saved = new SavedAnalysis(UUID.randomUUID(), userId, postId, null, OffsetDateTime.now(), null, null);
        when(savedAnalysisRepository.findDistinctUserIds()).thenReturn(List.of(userId));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(savedAnalysisRepository.findByUserId(userId)).thenReturn(List.of(saved));

        OffsetDateTime now = OffsetDateTime.now();
        when(buzzScoreHistoryRepository.findByPostIdOrderByCalculatedAtAsc(postId)).thenReturn(List.of(
                BuzzScoreHistoryEntry.of(postId, 60.0, Map.of(), now.minusDays(20))));

        int sentCount = service.sendDigests();

        assertThat(sentCount).isZero();
        verify(mailSenderPort, never()).sendWeeklyDigestEmail(anyString(), any(), anyDouble(), any(), anyInt());
    }

    @Test
    void sendDigests_skipsSilently_whenUserNoLongerExists() {
        when(savedAnalysisRepository.findDistinctUserIds()).thenReturn(List.of(userId));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        int sentCount = service.sendDigests();

        assertThat(sentCount).isZero();
        verify(mailSenderPort, never()).sendWeeklyDigestEmail(anyString(), any(), anyDouble(), any(), anyInt());
    }

    @Test
    void sendDigests_includesWeekOverWeekAverage_whenLastWeekDataExists() {
        SavedAnalysis saved = new SavedAnalysis(UUID.randomUUID(), userId, postId, null, OffsetDateTime.now(), null, null);
        when(savedAnalysisRepository.findDistinctUserIds()).thenReturn(List.of(userId));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(savedAnalysisRepository.findByUserId(userId)).thenReturn(List.of(saved));

        OffsetDateTime now = OffsetDateTime.now();
        when(buzzScoreHistoryRepository.findByPostIdOrderByCalculatedAtAsc(postId)).thenReturn(List.of(
                BuzzScoreHistoryEntry.of(postId, 50.0, Map.of(), now.minusDays(10)),
                BuzzScoreHistoryEntry.of(postId, 80.0, Map.of(), now.minusDays(1))));

        int sentCount = service.sendDigests();

        assertThat(sentCount).isEqualTo(1);
        verify(mailSenderPort).sendWeeklyDigestEmail(eq(user.getEmail()), any(), eq(80.0), eq(50.0), eq(1));
    }

    @Test
    void sendDigests_processesMultipleUsersIndependently() {
        UUID userId2 = UUID.randomUUID();
        UUID postId2 = UUID.randomUUID();
        User user2 = new User(userId2, "user2@example.com", "hash", "User2", Role.USER, true,
                OffsetDateTime.now(), OffsetDateTime.now());
        SavedAnalysis saved1 = new SavedAnalysis(UUID.randomUUID(), userId, postId, null, OffsetDateTime.now(), null, null);
        SavedAnalysis saved2 = new SavedAnalysis(UUID.randomUUID(), userId2, postId2, null, OffsetDateTime.now(), null, null);
        when(savedAnalysisRepository.findDistinctUserIds()).thenReturn(List.of(userId, userId2));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(userId2)).thenReturn(Optional.of(user2));
        when(savedAnalysisRepository.findByUserId(userId)).thenReturn(List.of(saved1));
        when(savedAnalysisRepository.findByUserId(userId2)).thenReturn(List.of(saved2));

        OffsetDateTime now = OffsetDateTime.now();
        when(buzzScoreHistoryRepository.findByPostIdOrderByCalculatedAtAsc(postId)).thenReturn(List.of(
                BuzzScoreHistoryEntry.of(postId, 65.0, Map.of(), now.minusDays(1))));
        when(buzzScoreHistoryRepository.findByPostIdOrderByCalculatedAtAsc(postId2)).thenReturn(List.of(
                BuzzScoreHistoryEntry.of(postId2, 75.0, Map.of(), now.minusDays(1))));

        int sentCount = service.sendDigests();

        assertThat(sentCount).isEqualTo(2);
        verify(mailSenderPort, times(2)).sendWeeklyDigestEmail(anyString(), any(), anyDouble(), any(), anyInt());
    }

    @Test
    void sendDigests_computesTopGenresPlatformWide_byNormalizedGenreAverage() {
        SavedAnalysis saved = new SavedAnalysis(UUID.randomUUID(), userId, postId, null, OffsetDateTime.now(), null, null);
        when(savedAnalysisRepository.findDistinctUserIds()).thenReturn(List.of(userId));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(savedAnalysisRepository.findByUserId(userId)).thenReturn(List.of(saved));
        when(buzzScoreHistoryRepository.findByPostIdOrderByCalculatedAtAsc(postId)).thenReturn(List.of(
                BuzzScoreHistoryEntry.of(postId, 65.0, Map.of(), OffsetDateTime.now().minusDays(1))));

        UUID beautyPostId = UUID.randomUUID();
        UUID cosmePostId = UUID.randomUUID();
        UUID foodPostId = UUID.randomUUID();
        Post beautyPost = post(beautyPostId);
        Post cosmePost = post(cosmePostId);
        Post foodPost = post(foodPostId);
        when(postRepository.search(any())).thenReturn(
                new PostSearchResult(List.of(beautyPost, cosmePost, foodPost), 0, 500, 3));
        when(analysisResultRepository.findByPostIdIn(any())).thenReturn(List.of(
                AnalysisResult.builder().id(UUID.randomUUID()).postId(beautyPostId).genre("美容").build(),
                AnalysisResult.builder().id(UUID.randomUUID()).postId(cosmePostId).genre("コスメ").build(),
                AnalysisResult.builder().id(UUID.randomUUID()).postId(foodPostId).genre("グルメ").build()));
        when(buzzScoreRepository.findByPostIdIn(any())).thenReturn(List.of(
                new BuzzScore(UUID.randomUUID(), beautyPostId, 80.0, Map.of(), OffsetDateTime.now()),
                new BuzzScore(UUID.randomUUID(), cosmePostId, 90.0, Map.of(), OffsetDateTime.now()),
                new BuzzScore(UUID.randomUUID(), foodPostId, 40.0, Map.of(), OffsetDateTime.now())));

        service.sendDigests();

        ArgumentCaptor<List<GenreScoreSummary>> topGenresCaptor = ArgumentCaptor.forClass(List.class);
        verify(mailSenderPort).sendWeeklyDigestEmail(anyString(), topGenresCaptor.capture(), anyDouble(), isNull(), anyInt());
        List<GenreScoreSummary> topGenres = topGenresCaptor.getValue();
        // 「美容」と「コスメ」はGenreNormalizerにより同一ジャンル(BEAUTY)として平均化される: (80+90)/2=85
        assertThat(topGenres).extracting(GenreScoreSummary::genre).containsExactly("BEAUTY", "FOOD");
        assertThat(topGenres.get(0).averageScore()).isEqualTo(85.0);
    }

    private Post post(UUID id) {
        UUID accountId = UUID.randomUUID();
        return new Post(id, accountId, Platform.INSTAGRAM, "ext-" + id, "https://example.com/" + id,
                OffsetDateTime.now(), "creator", "caption", List.of(), 10L, 2L, 100L, null, null, null,
                PostType.IMAGE, OffsetDateTime.now(), OffsetDateTime.now());
    }
}
