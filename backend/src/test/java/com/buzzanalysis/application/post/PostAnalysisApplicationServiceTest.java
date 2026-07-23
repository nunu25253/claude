package com.buzzanalysis.application.post;

import com.buzzanalysis.application.post.dto.AnalyzePostCommand;
import com.buzzanalysis.application.post.dto.AnalyzePostResult;
import com.buzzanalysis.domain.account.SocialAccount;
import com.buzzanalysis.domain.account.SocialAccountRepository;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.platform.FetchedPostData;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.platform.PlatformFactory;
import com.buzzanalysis.domain.platform.SocialPlatform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostSearchResult;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.score.BuzzScore;
import com.buzzanalysis.domain.score.BuzzScoreCalculator;
import com.buzzanalysis.domain.score.BuzzScoreHistoryRepository;
import com.buzzanalysis.domain.score.BuzzScoreRepository;
import com.buzzanalysis.domain.user.Role;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import com.buzzanalysis.infrastructure.quota.UsageQuotaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link PostAnalysisApplicationService} の単体テスト。
 * analysis_results.post_id / buzz_scores.post_id にはUNIQUE制約があるため、同じ投稿を2回分析しても
 * 制約違反にならず既存レコードのIDを引き継いで更新されることを検証する（実際にDocker Compose環境で
 * 同じ投稿URLを2回分析すると duplicate key で500になるバグが発覚したための回帰テスト）。
 */
@ExtendWith(MockitoExtension.class)
// 利用上限超過テストはsetUp()の大半のスタブ(投稿取得等)を使う前にanalyze()が例外を投げるため、
// 未使用スタブの厳格チェック(UnnecessaryStubbingException)を無効化する。
@MockitoSettings(strictness = Strictness.LENIENT)
class PostAnalysisApplicationServiceTest {

    @Mock
    private PlatformFactory platformFactory;
    @Mock
    private SocialAccountRepository socialAccountRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private AnalysisResultRepository analysisResultRepository;
    @Mock
    private BuzzScoreRepository buzzScoreRepository;
    @Mock
    private BuzzScoreHistoryRepository buzzScoreHistoryRepository;
    @Mock
    private AiPostAnalysisPort aiPostAnalysisPort;
    @Mock
    private BuzzScoreCalculator buzzScoreCalculator;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private SocialPlatform xClient;
    @Mock
    private UsageQuotaService usageQuotaService;
    @Mock
    private UserRepository userRepository;

    private PostAnalysisApplicationService service;
    private UUID postId;
    private UUID requestingUserId;
    private Post existingPost;

    @BeforeEach
    void setUp() {
        service = new PostAnalysisApplicationService(platformFactory, socialAccountRepository, postRepository,
                analysisResultRepository, buzzScoreRepository, buzzScoreHistoryRepository, aiPostAnalysisPort,
                buzzScoreCalculator, eventPublisher, usageQuotaService, userRepository);

        postId = UUID.randomUUID();
        requestingUserId = UUID.randomUUID();
        existingPost = new Post(postId, UUID.randomUUID(), Platform.X, "12345", "https://x.com/user/status/12345",
                OffsetDateTime.now(), "user", "caption", List.of(), 100L, 10L, 1000L, 5L, null, null,
                PostType.TEXT, OffsetDateTime.now(), OffsetDateTime.now());

        when(userRepository.findById(requestingUserId)).thenReturn(Optional.of(
                new User(requestingUserId, "user@example.com", "hash", "User", Role.USER, true,
                        OffsetDateTime.now(), OffsetDateTime.now())));
        when(usageQuotaService.tryConsume(any())).thenReturn(true);
        when(platformFactory.detectPlatformFromUrl(any())).thenReturn(Platform.X);
        when(platformFactory.resolve(Platform.X)).thenReturn(xClient);
        when(xClient.fetchPost(any())).thenReturn(Optional.of(fetchedPost()));
        when(socialAccountRepository.findByPlatformAndUsername(any(), any()))
                .thenReturn(Optional.of(SocialAccount.createNew(Platform.X, "ext", "user", "user", null, null, null)));
        when(postRepository.findByPlatformAndExternalId(Platform.X, "12345")).thenReturn(Optional.of(existingPost));
        when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(aiPostAnalysisPort.analyze(any())).thenReturn(aiOutput());
        when(buzzScoreCalculator.calculate(any()))
                .thenReturn(new BuzzScoreCalculator.CalculationResult(80.0, Map.of()));
        when(analysisResultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(buzzScoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(buzzScoreHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(postRepository.search(any())).thenReturn(new PostSearchResult(List.of(), 0, 6, 0));
    }

    @Test
    void analyze_reusesExistingAnalysisResultAndBuzzScoreIds_whenPostWasAlreadyAnalyzed() {
        UUID existingAnalysisResultId = UUID.randomUUID();
        UUID existingBuzzScoreId = UUID.randomUUID();
        when(analysisResultRepository.findByPostId(postId)).thenReturn(Optional.of(
                AnalysisResult.builder().id(existingAnalysisResultId).postId(postId).genre("old genre").build()));
        when(buzzScoreRepository.findByPostId(postId)).thenReturn(Optional.of(
                new BuzzScore(existingBuzzScoreId, postId, 50.0, Map.of(), OffsetDateTime.now())));

        AnalyzePostResult result = service.analyze(new AnalyzePostCommand("https://x.com/user/status/12345", requestingUserId));

        ArgumentCaptor<AnalysisResult> analysisCaptor = ArgumentCaptor.forClass(AnalysisResult.class);
        ArgumentCaptor<BuzzScore> buzzScoreCaptor = ArgumentCaptor.forClass(BuzzScore.class);
        org.mockito.Mockito.verify(analysisResultRepository).save(analysisCaptor.capture());
        org.mockito.Mockito.verify(buzzScoreRepository).save(buzzScoreCaptor.capture());

        assertThat(analysisCaptor.getValue().getId()).isEqualTo(existingAnalysisResultId);
        assertThat(buzzScoreCaptor.getValue().getId()).isEqualTo(existingBuzzScoreId);
        assertThat(buzzScoreCaptor.getValue().getTotalScore()).isEqualTo(80.0);
        assertThat(result.analysis()).isNotNull();
    }

    @Test
    void analyze_createsNewAnalysisResultAndBuzzScore_whenPostNeverAnalyzedBefore() {
        when(analysisResultRepository.findByPostId(postId)).thenReturn(Optional.empty());
        when(buzzScoreRepository.findByPostId(postId)).thenReturn(Optional.empty());

        service.analyze(new AnalyzePostCommand("https://x.com/user/status/12345", requestingUserId));

        ArgumentCaptor<AnalysisResult> analysisCaptor = ArgumentCaptor.forClass(AnalysisResult.class);
        ArgumentCaptor<BuzzScore> buzzScoreCaptor = ArgumentCaptor.forClass(BuzzScore.class);
        org.mockito.Mockito.verify(analysisResultRepository).save(analysisCaptor.capture());
        org.mockito.Mockito.verify(buzzScoreRepository).save(buzzScoreCaptor.capture());

        assertThat(analysisCaptor.getValue().getId()).isNotNull();
        assertThat(buzzScoreCaptor.getValue().getId()).isNotNull();
    }

    @Test
    void analyze_appendsBuzzScoreHistoryEntry_onEveryAnalysis() {
        when(analysisResultRepository.findByPostId(postId)).thenReturn(Optional.of(
                AnalysisResult.builder().id(UUID.randomUUID()).postId(postId).genre("old genre").build()));
        when(buzzScoreRepository.findByPostId(postId)).thenReturn(Optional.of(
                new BuzzScore(UUID.randomUUID(), postId, 50.0, Map.of(), OffsetDateTime.now())));

        service.analyze(new AnalyzePostCommand("https://x.com/user/status/12345", requestingUserId));

        ArgumentCaptor<com.buzzanalysis.domain.score.BuzzScoreHistoryEntry> historyCaptor =
                ArgumentCaptor.forClass(com.buzzanalysis.domain.score.BuzzScoreHistoryEntry.class);
        org.mockito.Mockito.verify(buzzScoreHistoryRepository).save(historyCaptor.capture());

        assertThat(historyCaptor.getValue().getPostId()).isEqualTo(postId);
        assertThat(historyCaptor.getValue().getTotalScore()).isEqualTo(80.0);
        // buzz_scoresはUPSERT(既存IDを引き継ぐ)だが、履歴は毎回新しいIDで追記されること
        assertThat(historyCaptor.getValue().getId()).isNotNull();
    }

    @Test
    void analyze_throwsBusinessRuleViolation_whenDailyUsageQuotaExceeded() {
        when(usageQuotaService.tryConsume(requestingUserId)).thenReturn(false);

        org.junit.jupiter.api.Assertions.assertThrows(BusinessRuleViolationException.class,
                () -> service.analyze(new AnalyzePostCommand("https://x.com/user/status/12345", requestingUserId)));

        org.mockito.Mockito.verifyNoInteractions(platformFactory);
    }

    @Test
    void analyze_throwsBusinessRuleViolation_whenEmailNotVerified() {
        when(userRepository.findById(requestingUserId)).thenReturn(Optional.of(
                new User(requestingUserId, "user@example.com", "hash", "User", Role.USER, false,
                        OffsetDateTime.now(), OffsetDateTime.now())));

        org.junit.jupiter.api.Assertions.assertThrows(BusinessRuleViolationException.class,
                () -> service.analyze(new AnalyzePostCommand("https://x.com/user/status/12345", requestingUserId)));

        org.mockito.Mockito.verifyNoInteractions(usageQuotaService);
        org.mockito.Mockito.verifyNoInteractions(platformFactory);
    }

    private FetchedPostData fetchedPost() {
        return new FetchedPostData("12345", "https://x.com/user/status/12345", OffsetDateTime.now(), "user",
                "caption", List.of(), 100L, 10L, 1000L, 5L, null, null, PostType.TEXT);
    }

    private AiPostAnalysisPort.AiAnalysisOutput aiOutput() {
        return new AiPostAnalysisPort.AiAnalysisOutput(
                "genre", "subGenre", "why", "target", "purpose", "hook", "cta", "structure",
                "sentiment", 0.5, null, null, "title", "text", "timing", "hashtag",
                "strengths", "weaknesses", "improvements", 0.5);
    }
}
