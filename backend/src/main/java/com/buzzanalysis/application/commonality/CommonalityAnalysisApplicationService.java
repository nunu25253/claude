package com.buzzanalysis.application.commonality;

import com.buzzanalysis.application.commonality.dto.CommonalityAnalysisResultDto;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.commonality.CommonalityAnalysisResult;
import com.buzzanalysis.domain.commonality.CommonalityStatisticsCalculator;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.normalization.PostNormalizer;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.preprocessing.PostPreprocessor;
import com.buzzanalysis.domain.preprocessing.PreprocessedPost;
import com.buzzanalysis.infrastructure.quota.UsageQuotaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 「共通点分析」ユースケース（Phase8）。検索結果等の投稿群から、統計的な共通項目
 * （ハッシュタグ/動画時間/投稿時間/コンテンツ形式）とAIによる共通パターン（タイトル/フック/CTA/構成/ターゲット）
 * を抽出する。永続化はしない（都度計算するオンデマンド分析、Phase4/6と同方針）。
 */
@Service
public class CommonalityAnalysisApplicationService {

    /** 入力投稿数の上限（要求仕様の「検索結果100件」を想定した上限）。超過分は先頭から切り詰める。 */
    private static final int MAX_POSTS = 100;
    /** AIプロンプトに含めるサンプル数の上限（トークン数・コスト抑制のため）。 */
    private static final int AI_SAMPLE_SIZE = 30;
    private static final int SNIPPET_MAX_LENGTH = 80;

    private final PostRepository postRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final PostNormalizer postNormalizer;
    private final PostPreprocessor postPreprocessor;
    private final CommonalityStatisticsCalculator statisticsCalculator;
    private final AiCommonalityAnalysisPort aiCommonalityAnalysisPort;
    private final UsageQuotaService usageQuotaService;

    public CommonalityAnalysisApplicationService(PostRepository postRepository,
                                                  AnalysisResultRepository analysisResultRepository,
                                                  PostNormalizer postNormalizer,
                                                  PostPreprocessor postPreprocessor,
                                                  CommonalityStatisticsCalculator statisticsCalculator,
                                                  AiCommonalityAnalysisPort aiCommonalityAnalysisPort,
                                                  UsageQuotaService usageQuotaService) {
        this.postRepository = postRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.postNormalizer = postNormalizer;
        this.postPreprocessor = postPreprocessor;
        this.statisticsCalculator = statisticsCalculator;
        this.aiCommonalityAnalysisPort = aiCommonalityAnalysisPort;
        this.usageQuotaService = usageQuotaService;
    }

    @Transactional(readOnly = true)
    public CommonalityAnalysisResultDto analyze(List<UUID> postIds, UUID requestingUserId) {
        if (!usageQuotaService.tryConsume(requestingUserId)) {
            throw new BusinessRuleViolationException("本日のAI機能の利用回数上限に達しました。明日以降に再度お試しください。",
                    UsageQuotaService.EXCEEDED_ERROR_CODE);
        }
        List<UUID> targetIds = postIds.size() > MAX_POSTS ? postIds.subList(0, MAX_POSTS) : postIds;

        // 件数分のfindByIdをループで叩くN+1クエリになっていたため、まとめてバッチ取得しtargetIdsの順序で並べ直す
        // (SavedAnalysisApplicationService.listと同じ方針)。
        Map<UUID, Post> postsById = postRepository.findByIdIn(targetIds).stream()
                .collect(Collectors.toMap(Post::getId, Function.identity()));
        List<Post> posts = targetIds.stream().map(postsById::get).filter(Objects::nonNull).toList();

        List<PreprocessedPost> preprocessedPosts = posts.stream()
                .map(post -> postPreprocessor.preprocess(postNormalizer.normalize(post)))
                .toList();

        List<AiCommonalityAnalysisPort.PostSnippet> snippets = buildSnippets(posts);
        AiCommonalityAnalysisPort.AiCommonalityOutput aiOutput = aiCommonalityAnalysisPort.analyze(snippets);

        CommonalityAnalysisResult result = CommonalityAnalysisResult.builder()
                .totalPostCount(posts.size())
                .aiSampleSize(snippets.size())
                .commonHashtags(statisticsCalculator.topHashtags(preprocessedPosts))
                .commonVideoDurationSeconds(statisticsCalculator.medianVideoDurationSeconds(preprocessedPosts))
                .commonPostingHour(statisticsCalculator.mostCommonPostingHour(preprocessedPosts))
                .commonContentFormat(statisticsCalculator.mostCommonContentFormat(preprocessedPosts))
                .commonTitlePattern(aiOutput.commonTitlePattern())
                .commonHookPattern(aiOutput.commonHookPattern())
                .commonCtaPattern(aiOutput.commonCtaPattern())
                .commonStructurePattern(aiOutput.commonStructurePattern())
                .commonTargetPattern(aiOutput.commonTargetPattern())
                .build();

        return CommonalityAnalysisResultDto.from(result);
    }

    private List<AiCommonalityAnalysisPort.PostSnippet> buildSnippets(List<Post> posts) {
        List<Post> candidatePosts = posts.size() > AI_SAMPLE_SIZE ? posts.subList(0, AI_SAMPLE_SIZE) : posts;
        List<UUID> candidateIds = candidatePosts.stream().map(Post::getId).toList();
        Map<UUID, AnalysisResult> analysisResultsByPostId = analysisResultRepository.findByPostIdIn(candidateIds).stream()
                .collect(Collectors.toMap(AnalysisResult::getPostId, Function.identity()));

        List<AiCommonalityAnalysisPort.PostSnippet> snippets = new ArrayList<>();
        for (Post post : candidatePosts) {
            AnalysisResult ar = analysisResultsByPostId.get(post.getId());
            if (ar != null) {
                snippets.add(new AiCommonalityAnalysisPort.PostSnippet(
                        truncate(ar.getTitleAnalysis()),
                        truncate(ar.getHook()),
                        truncate(ar.getCallToAction()),
                        truncate(ar.getPostStructureAnalysis()),
                        truncate(ar.getTargetAudience())
                ));
            }
        }
        return snippets;
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= SNIPPET_MAX_LENGTH ? value : value.substring(0, SNIPPET_MAX_LENGTH) + "...";
    }
}
