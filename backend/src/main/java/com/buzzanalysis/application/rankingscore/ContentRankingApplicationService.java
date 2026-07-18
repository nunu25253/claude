package com.buzzanalysis.application.rankingscore;

import com.buzzanalysis.application.matching.UserConditionMatchApplicationService;
import com.buzzanalysis.application.matching.dto.MatchRateResultDto;
import com.buzzanalysis.application.post.dto.PostDto;
import com.buzzanalysis.application.rankingscore.dto.RankingScoreResultDto;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.matching.UserSearchCondition;
import com.buzzanalysis.domain.normalization.NormalizedPost;
import com.buzzanalysis.domain.normalization.PostNormalizer;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.preprocessing.PostPreprocessor;
import com.buzzanalysis.domain.preprocessing.PreprocessedPost;
import com.buzzanalysis.domain.rankingscore.RankingScoreCalculator;
import com.buzzanalysis.domain.rankingscore.RankingScoreInput;
import com.buzzanalysis.domain.score.BuzzScore;
import com.buzzanalysis.domain.score.BuzzScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 「ランキングAI」ユースケース（Phase7）。Phase6のユーザー条件分析結果（一致率付き候補）に対し、
 * 既存のBuzzScore・いいね率・投稿鮮度・動画時間を加味した総合ランキングスコアで再順位付けする。
 */
@Service
public class ContentRankingApplicationService {

    private final UserConditionMatchApplicationService userConditionMatchApplicationService;
    private final PostRepository postRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final BuzzScoreRepository buzzScoreRepository;
    private final PostNormalizer postNormalizer;
    private final PostPreprocessor postPreprocessor;
    private final RankingScoreCalculator rankingScoreCalculator;

    public ContentRankingApplicationService(UserConditionMatchApplicationService userConditionMatchApplicationService,
                                             PostRepository postRepository,
                                             AnalysisResultRepository analysisResultRepository,
                                             BuzzScoreRepository buzzScoreRepository,
                                             PostNormalizer postNormalizer,
                                             PostPreprocessor postPreprocessor,
                                             RankingScoreCalculator rankingScoreCalculator) {
        this.userConditionMatchApplicationService = userConditionMatchApplicationService;
        this.postRepository = postRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.buzzScoreRepository = buzzScoreRepository;
        this.postNormalizer = postNormalizer;
        this.postPreprocessor = postPreprocessor;
        this.rankingScoreCalculator = rankingScoreCalculator;
    }

    @Transactional(readOnly = true)
    public List<RankingScoreResultDto> rank(UserSearchCondition condition, int limit) {
        int candidatePoolSize = Math.max(limit * 3, 30);
        List<MatchRateResultDto> matched = userConditionMatchApplicationService.evaluate(condition, candidatePoolSize);

        List<RankingScoreResultDto> results = new ArrayList<>();
        for (MatchRateResultDto match : matched) {
            UUID postId = match.post().id();
            Optional<Post> post = postRepository.findById(postId);
            if (post.isEmpty()) {
                continue;
            }
            results.add(scoreOne(post.get(), match.matchRatePercent()));
        }

        return results.stream()
                .sorted(Comparator.comparingDouble(RankingScoreResultDto::rankingScore).reversed())
                .limit(limit)
                .toList();
    }

    private RankingScoreResultDto scoreOne(Post post, double matchRatePercent) {
        AnalysisResult analysisResult = analysisResultRepository.findByPostId(post.getId()).orElse(null);
        BuzzScore buzzScore = buzzScoreRepository.findByPostId(post.getId()).orElse(null);
        NormalizedPost normalizedPost = postNormalizer.normalize(post);
        PreprocessedPost preprocessedPost = postPreprocessor.preprocess(normalizedPost);

        RankingScoreInput input = new RankingScoreInput(post, analysisResult, preprocessedPost, buzzScore, matchRatePercent);
        RankingScoreCalculator.CalculationResult calculation = rankingScoreCalculator.calculate(input);

        return new RankingScoreResultDto(PostDto.from(post), calculation.rankingScore(), matchRatePercent,
                calculation.breakdown());
    }
}
