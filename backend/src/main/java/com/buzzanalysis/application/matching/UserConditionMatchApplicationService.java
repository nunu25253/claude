package com.buzzanalysis.application.matching;

import com.buzzanalysis.application.matching.dto.MatchRateResultDto;
import com.buzzanalysis.application.post.dto.PostDto;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.embedding.EmbeddingClient;
import com.buzzanalysis.domain.embedding.EmbeddingRepository;
import com.buzzanalysis.domain.embedding.EmbeddingResult;
import com.buzzanalysis.domain.embedding.EmbeddingTarget;
import com.buzzanalysis.domain.embedding.SimilarityMatch;
import com.buzzanalysis.domain.matching.MatchRateCalculator;
import com.buzzanalysis.domain.matching.MatchRateInput;
import com.buzzanalysis.domain.matching.UserSearchCondition;
import com.buzzanalysis.domain.normalization.NormalizedPost;
import com.buzzanalysis.domain.normalization.PostNormalizer;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostSearchCriteria;
import com.buzzanalysis.domain.preprocessing.PostPreprocessor;
import com.buzzanalysis.domain.preprocessing.PreprocessedPost;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 「ユーザー条件分析」ユースケース（Phase6）。ユーザーが指定した条件（キーワード・ジャンル・ターゲット等）と
 * 各投稿の一致率を算出する。自由文条件（キーワード/商品名/ブランド/ASP案件名）が指定されている場合は
 * Phase4の意味検索で候補を絞り込み、指定が無い場合は通常の投稿検索（プラットフォームのみフィルタ）で
 * 候補を取得する。プラットフォーム指定はハードフィルタとして扱う
 * （docs/phases/phase6_condition_matching.md 参照）。
 */
@Service
public class UserConditionMatchApplicationService {

    private final EmbeddingClient embeddingClient;
    private final EmbeddingRepository embeddingRepository;
    private final PostRepository postRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final PostNormalizer postNormalizer;
    private final PostPreprocessor postPreprocessor;
    private final MatchRateCalculator matchRateCalculator;

    public UserConditionMatchApplicationService(EmbeddingClient embeddingClient,
                                                 EmbeddingRepository embeddingRepository,
                                                 PostRepository postRepository,
                                                 AnalysisResultRepository analysisResultRepository,
                                                 PostNormalizer postNormalizer,
                                                 PostPreprocessor postPreprocessor,
                                                 MatchRateCalculator matchRateCalculator) {
        this.embeddingClient = embeddingClient;
        this.embeddingRepository = embeddingRepository;
        this.postRepository = postRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.postNormalizer = postNormalizer;
        this.postPreprocessor = postPreprocessor;
        this.matchRateCalculator = matchRateCalculator;
    }

    @Transactional(readOnly = true)
    public List<MatchRateResultDto> evaluate(UserSearchCondition condition, int limit) {
        int candidatePoolSize = Math.max(limit * 5, 50);
        List<CandidatePost> candidates = collectCandidates(condition, candidatePoolSize);

        // 候補件数分のfindById/findByPostIdをループで叩くN+1クエリになっていたため、まとめてバッチ取得する
        // （SavedAnalysisApplicationService.listと同じ方針）。
        List<UUID> candidateIds = candidates.stream().map(CandidatePost::postId).toList();
        Map<UUID, Post> postsById = postRepository.findByIdIn(candidateIds).stream()
                .collect(Collectors.toMap(Post::getId, Function.identity()));
        Map<UUID, AnalysisResult> analysisResultsByPostId = analysisResultRepository.findByPostIdIn(candidateIds).stream()
                .collect(Collectors.toMap(AnalysisResult::getPostId, Function.identity()));

        List<MatchRateResultDto> results = new ArrayList<>();
        for (CandidatePost candidate : candidates) {
            Post post = postsById.get(candidate.postId());
            if (post == null) {
                continue;
            }
            if (condition.getPlatform() != null && post.getPlatform() != condition.getPlatform()) {
                continue;
            }
            results.add(evaluateOne(post, analysisResultsByPostId.get(post.getId()), condition, candidate.similarity()));
        }

        return results.stream()
                .sorted(Comparator.comparingDouble(MatchRateResultDto::matchRatePercent).reversed())
                .limit(limit)
                .toList();
    }

    private MatchRateResultDto evaluateOne(Post post, AnalysisResult analysisResult, UserSearchCondition condition, Double similarity) {
        NormalizedPost normalizedPost = postNormalizer.normalize(post);
        PreprocessedPost preprocessedPost = postPreprocessor.preprocess(normalizedPost);

        MatchRateInput input = new MatchRateInput(post, analysisResult, preprocessedPost, condition, similarity);
        MatchRateCalculator.CalculationResult calculation = matchRateCalculator.calculate(input);

        return new MatchRateResultDto(PostDto.from(post), calculation.matchRatePercent(), calculation.breakdown());
    }

    private List<CandidatePost> collectCandidates(UserSearchCondition condition, int candidatePoolSize) {
        String queryText = condition.toSemanticQueryText();
        if (!queryText.isBlank()) {
            EmbeddingResult queryEmbedding = embeddingClient.embed(queryText);
            List<SimilarityMatch> matches = embeddingRepository.findNearest(
                    EmbeddingTarget.BODY, queryEmbedding.vector(), candidatePoolSize);
            return matches.stream().map(m -> new CandidatePost(m.postId(), m.similarity())).toList();
        }

        PostSearchCriteria criteria = new PostSearchCriteria(null, null, null, condition.getPlatform(),
                0, candidatePoolSize, "publishedAt", false);
        return postRepository.search(criteria).content().stream()
                .map(p -> new CandidatePost(p.getId(), null))
                .toList();
    }

    private record CandidatePost(UUID postId, Double similarity) {
    }
}
