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
import com.buzzanalysis.domain.score.BuzzScoreInput;
import com.buzzanalysis.domain.score.BuzzScoreRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 「投稿URL分析」ユースケース：URL受け取り → 公開データ取得 → AI分析 → BuzzScore算出 → 類似投稿提案、を一気通貫で行う。
 * 分析完了時には {@link AnalysisCompletedEvent} を発行し、Observer（レポート生成トリガー/キャッシュ更新）に通知する。
 */
@Service
public class PostAnalysisApplicationService {

    private final PlatformFactory platformFactory;
    private final SocialAccountRepository socialAccountRepository;
    private final PostRepository postRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final BuzzScoreRepository buzzScoreRepository;
    private final AiPostAnalysisPort aiPostAnalysisPort;
    private final BuzzScoreCalculator buzzScoreCalculator;
    private final ApplicationEventPublisher eventPublisher;

    public PostAnalysisApplicationService(PlatformFactory platformFactory,
                                           SocialAccountRepository socialAccountRepository,
                                           PostRepository postRepository,
                                           AnalysisResultRepository analysisResultRepository,
                                           BuzzScoreRepository buzzScoreRepository,
                                           AiPostAnalysisPort aiPostAnalysisPort,
                                           BuzzScoreCalculator buzzScoreCalculator,
                                           ApplicationEventPublisher eventPublisher) {
        this.platformFactory = platformFactory;
        this.socialAccountRepository = socialAccountRepository;
        this.postRepository = postRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.buzzScoreRepository = buzzScoreRepository;
        this.aiPostAnalysisPort = aiPostAnalysisPort;
        this.buzzScoreCalculator = buzzScoreCalculator;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AnalyzePostResult analyze(AnalyzePostCommand command) {
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
        BuzzScore buzzScore = BuzzScore.of(post.getId(), calculation.totalScore(), calculation.breakdown());
        buzzScoreRepository.save(buzzScore);

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
        return AnalysisResult.builder()
                .postId(postId)
                .whyItWentViral(out.whyItWentViral())
                .targetAudience(out.targetAudience())
                .hook(out.hook())
                .callToAction(out.callToAction())
                .sentimentAnalysis(out.sentimentAnalysis())
                .videoStructureAnalysis(out.videoStructureAnalysis())
                .carouselStructureAnalysis(out.carouselStructureAnalysis())
                .titleAnalysis(out.titleAnalysis())
                .textAnalysis(out.textAnalysis())
                .postingTimeAnalysis(out.postingTimeAnalysis())
                .hashtagAnalysis(out.hashtagAnalysis())
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
