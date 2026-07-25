package com.buzzanalysis.application.post;

import com.buzzanalysis.application.post.dto.AnalysisResultDto;
import com.buzzanalysis.application.post.dto.AnalyzePostCommand;
import com.buzzanalysis.application.post.dto.AnalyzePostResult;
import com.buzzanalysis.application.post.dto.BuzzScoreDto;
import com.buzzanalysis.application.post.dto.PostDto;
import com.buzzanalysis.domain.account.SocialAccount;
import com.buzzanalysis.domain.account.SocialAccountRepository;
import com.buzzanalysis.domain.analysis.AnalysisCompletedEvent;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.platform.FetchedAccountData;
import com.buzzanalysis.domain.platform.FetchedPostData;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.platform.PlatformFactory;
import com.buzzanalysis.domain.platform.SocialPlatform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostSearchCriteria;
import com.buzzanalysis.domain.score.BuzzScore;
import com.buzzanalysis.domain.score.BuzzScoreCalculator;
import com.buzzanalysis.domain.score.BuzzScoreHistoryEntry;
import com.buzzanalysis.domain.score.BuzzScoreHistoryRepository;
import com.buzzanalysis.domain.score.BuzzScoreInput;
import com.buzzanalysis.domain.score.BuzzScoreRepository;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import com.buzzanalysis.infrastructure.quota.UsageQuotaService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 「投稿URL分析」ユースケース：URL受け取り → 公開データ取得 → AI分析 → BuzzScore算出 → 類似投稿提案、を一気通貫で行う。
 * 分析完了時には {@link AnalysisCompletedEvent} を発行し、Observer（レポート生成トリガー/キャッシュ更新）に通知する。
 */
@Service
public class PostAnalysisApplicationService {

    /**
     * メール未確認かつ無料お試し分析を既に使い切っている場合にフロントエンドが判別するためのerrorCode。
     * (改善計画: 登録直後にメール確認を必須にすると初回体験の離脱率が上がるため、1回だけ未確認でも
     * 試せるようにした。2回目以降はこのコードを見てフロントエンドが確認メール再送を促すCTAを出す。)
     */
    public static final String EMAIL_NOT_VERIFIED_CODE = "EMAIL_NOT_VERIFIED";

    private final PlatformFactory platformFactory;
    private final SocialAccountRepository socialAccountRepository;
    private final PostRepository postRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final BuzzScoreRepository buzzScoreRepository;
    private final BuzzScoreHistoryRepository buzzScoreHistoryRepository;
    private final AiPostAnalysisPort aiPostAnalysisPort;
    private final BuzzScoreCalculator buzzScoreCalculator;
    private final ApplicationEventPublisher eventPublisher;
    private final UsageQuotaService usageQuotaService;
    private final UserRepository userRepository;

    public PostAnalysisApplicationService(PlatformFactory platformFactory,
                                           SocialAccountRepository socialAccountRepository,
                                           PostRepository postRepository,
                                           AnalysisResultRepository analysisResultRepository,
                                           BuzzScoreRepository buzzScoreRepository,
                                           BuzzScoreHistoryRepository buzzScoreHistoryRepository,
                                           AiPostAnalysisPort aiPostAnalysisPort,
                                           BuzzScoreCalculator buzzScoreCalculator,
                                           ApplicationEventPublisher eventPublisher,
                                           UsageQuotaService usageQuotaService,
                                           UserRepository userRepository) {
        this.platformFactory = platformFactory;
        this.socialAccountRepository = socialAccountRepository;
        this.postRepository = postRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.buzzScoreRepository = buzzScoreRepository;
        this.buzzScoreHistoryRepository = buzzScoreHistoryRepository;
        this.aiPostAnalysisPort = aiPostAnalysisPort;
        this.buzzScoreCalculator = buzzScoreCalculator;
        this.eventPublisher = eventPublisher;
        this.usageQuotaService = usageQuotaService;
        this.userRepository = userRepository;
    }

    @Transactional
    public AnalyzePostResult analyze(AnalyzePostCommand command) {
        User requestingUser = userRepository.findById(command.requestingUserId())
                .orElseThrow(() -> EntityNotFoundException.of("User", command.requestingUserId()));
        if (!requestingUser.isEmailVerified()) {
            if (requestingUser.hasUsedTrialAnalysis()) {
                throw new BusinessRuleViolationException(
                        "無料お試し分析は既にご利用いただきました。引き続きご利用いただくには、"
                                + "登録時に送信された確認メールのリンクからメールアドレスをご確認ください。",
                        EMAIL_NOT_VERIFIED_CODE);
            }
            // メール未確認でも1回だけ体験できるようにする(登録直後にメール確認を必須にすると
            // 初回体験の離脱率が上がるため)。ここで消費を記録してから分析を続行する。
            requestingUser.markTrialAnalysisUsed();
            userRepository.save(requestingUser);
        }
        if (!usageQuotaService.tryConsume(command.requestingUserId())) {
            throw new BusinessRuleViolationException("本日の投稿分析の利用回数上限に達しました。明日以降に再度お試しください。",
                    UsageQuotaService.EXCEEDED_ERROR_CODE);
        }

        Platform platform = platformFactory.detectPlatformFromUrl(command.postUrl());
        SocialPlatform client = platformFactory.resolve(platform);

        FetchedPostData fetchedPost = client.fetchPost(command.postUrl())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Post could not be fetched from " + platform + ": " + command.postUrl()));

        SocialAccount account = resolveOrCreateAccount(client, platform, fetchedPost);
        Post post = saveOrUpdatePost(account, platform, fetchedPost);

        AiPostAnalysisPort.AiAnalysisOutput aiOutput = aiPostAnalysisPort.analyze(post);
        AnalysisResult analysisResult = buildAnalysisResult(post.getId(), aiOutput);
        analysisResultRepository.save(analysisResult);

        BuzzScoreInput scoreInput = new BuzzScoreInput(post, aiOutput.sentimentScore(), aiOutput.viralPotentialHint());
        BuzzScoreCalculator.CalculationResult calculation = buzzScoreCalculator.calculate(scoreInput);
        UUID existingBuzzScoreId = buzzScoreRepository.findByPostId(post.getId()).map(BuzzScore::getId).orElse(null);
        BuzzScore buzzScore = existingBuzzScoreId != null
                ? new BuzzScore(existingBuzzScoreId, post.getId(), calculation.totalScore(), calculation.breakdown(),
                        OffsetDateTime.now())
                : BuzzScore.of(post.getId(), calculation.totalScore(), calculation.breakdown());
        buzzScoreRepository.save(buzzScore);
        // buzz_scoresは最新値のみ保持(再分析でUPSERT)するため、時系列比較用に履歴テーブルへも追記する。
        buzzScoreHistoryRepository.save(
                BuzzScoreHistoryEntry.of(post.getId(), calculation.totalScore(), calculation.breakdown(), buzzScore.getCalculatedAt()));

        eventPublisher.publishEvent(new AnalysisCompletedEvent(post.getId(), analysisResult.getId(), calculation.totalScore()));

        List<PostDto> similarPosts = findSimilarPosts(post);

        return new AnalyzePostResult(
                PostDto.from(post),
                AnalysisResultDto.from(analysisResult),
                BuzzScoreDto.from(buzzScore),
                similarPosts
        );
    }

    private SocialAccount resolveOrCreateAccount(SocialPlatform client, Platform platform, FetchedPostData fetchedPost) {
        String username = fetchedPost.authorName();
        Optional<SocialAccount> existing = socialAccountRepository.findByPlatformAndUsername(platform, username);
        if (existing.isPresent()) {
            return existing.get();
        }

        Optional<FetchedAccountData> accountData = client.fetchAccount(username);
        SocialAccount account = accountData
                .map(data -> SocialAccount.createNew(platform, data.externalAccountId(), data.username(),
                        data.displayName(), data.profileUrl(), data.followerCount(), data.postCount()))
                .orElseGet(() -> SocialAccount.createNew(platform, username, username, username, null, null, null));
        return socialAccountRepository.save(account);
    }

    private Post saveOrUpdatePost(SocialAccount account, Platform platform, FetchedPostData fetchedPost) {
        Optional<Post> existing = postRepository.findByPlatformAndExternalId(platform, fetchedPost.externalId());
        if (existing.isPresent()) {
            Post post = existing.get();
            post.refreshMetrics(fetchedPost);
            return postRepository.save(post);
        }
        Post newPost = Post.fromFetchedData(account.getId(), platform, fetchedPost);
        return postRepository.save(newPost);
    }

    private AnalysisResult buildAnalysisResult(UUID postId, AiPostAnalysisPort.AiAnalysisOutput out) {
        // analysis_results.post_id にはUNIQUE制約があるため、既存レコードがあればそのIDを引き継いで
        // 更新する（同じ投稿を再分析すると制約違反になってしまうため）。
        UUID existingId = analysisResultRepository.findByPostId(postId).map(AnalysisResult::getId).orElse(null);
        return AnalysisResult.builder()
                .id(existingId)
                .postId(postId)
                .genre(out.genre())
                .subGenre(out.subGenre())
                .whyItWentViral(out.whyItWentViral())
                .targetAudience(out.targetAudience())
                .postPurpose(out.postPurpose())
                .hook(out.hook())
                .callToAction(out.callToAction())
                .postStructureAnalysis(out.postStructureAnalysis())
                .sentimentAnalysis(out.sentimentAnalysis())
                .videoStructureAnalysis(out.videoStructureAnalysis())
                .carouselStructureAnalysis(out.carouselStructureAnalysis())
                .titleAnalysis(out.titleAnalysis())
                .textAnalysis(out.textAnalysis())
                .postingTimeAnalysis(out.postingTimeAnalysis())
                .hashtagAnalysis(out.hashtagAnalysis())
                .strengths(out.strengths())
                .weaknesses(out.weaknesses())
                .improvementSuggestions(out.improvementSuggestions())
                .build();
    }

    /** 同一プラットフォーム・同一ハッシュタグを持つ他の投稿を類似投稿として最大5件提案する。 */
    private List<PostDto> findSimilarPosts(Post post) {
        String hashtag = post.getHashtags() == null || post.getHashtags().isEmpty() ? null : post.getHashtags().get(0);
        PostSearchCriteria criteria = new PostSearchCriteria(null, hashtag, null, post.getPlatform(),
                0, 6, "likeCount", false);
        return postRepository.search(criteria).content().stream()
                .filter(p -> !p.getId().equals(post.getId()))
                .limit(5)
                .map(PostDto::from)
                .toList();
    }
}
