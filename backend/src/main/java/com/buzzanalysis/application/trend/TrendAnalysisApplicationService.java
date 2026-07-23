package com.buzzanalysis.application.trend;

import com.buzzanalysis.application.trend.dto.TrendAnalysisRequest;
import com.buzzanalysis.application.trend.dto.TrendReportDto;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.normalization.PostNormalizer;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostSearchCriteria;
import com.buzzanalysis.domain.preprocessing.ContentFormat;
import com.buzzanalysis.domain.preprocessing.PostPreprocessor;
import com.buzzanalysis.domain.preprocessing.PreprocessedPost;
import com.buzzanalysis.domain.trend.TrendCategory;
import com.buzzanalysis.domain.trend.TrendItem;
import com.buzzanalysis.domain.trend.TrendReport;
import com.buzzanalysis.domain.trend.TrendReportRepository;
import com.buzzanalysis.infrastructure.quota.UsageQuotaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 「トレンド分析」ユースケース（Phase15）。直近ウィンドウとベースラインウィンドウの2期間で
 * ハッシュタグ/ジャンル/コンテンツ形式の出現件数を比較し、急上昇している項目を決定的な統計計算で
 * 検出する（AIはサマリー生成のみに使用）。
 */
@Service
public class TrendAnalysisApplicationService {

    private static final int MAX_POSTS_FOR_AGGREGATION = 500;
    private static final int DEFAULT_RECENT_WINDOW_DAYS = 7;
    private static final int DEFAULT_BASELINE_WINDOW_DAYS = 21;
    private static final int MIN_RECENT_SAMPLE_SIZE = 3;
    private static final int TOP_N_PER_CATEGORY = 5;

    private final PostRepository postRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final PostNormalizer postNormalizer;
    private final PostPreprocessor postPreprocessor;
    private final AiTrendSummaryPort aiTrendSummaryPort;
    private final TrendReportRepository trendReportRepository;
    private final UsageQuotaService usageQuotaService;

    public TrendAnalysisApplicationService(PostRepository postRepository,
                                            AnalysisResultRepository analysisResultRepository,
                                            PostNormalizer postNormalizer,
                                            PostPreprocessor postPreprocessor,
                                            AiTrendSummaryPort aiTrendSummaryPort,
                                            TrendReportRepository trendReportRepository,
                                            UsageQuotaService usageQuotaService) {
        this.postRepository = postRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.postNormalizer = postNormalizer;
        this.postPreprocessor = postPreprocessor;
        this.aiTrendSummaryPort = aiTrendSummaryPort;
        this.trendReportRepository = trendReportRepository;
        this.usageQuotaService = usageQuotaService;
    }

    /**
     * requestingUserIdはnull許容。ユーザー操作(API経由)では必須だが、スケジューラによる
     * 定期実行(誰の操作でもない)ではnullを渡し、その場合は利用上限チェックをスキップする。
     */
    @Transactional
    public TrendReportDto analyze(TrendAnalysisRequest request, UUID requestingUserId) {
        if (requestingUserId != null && !usageQuotaService.tryConsume(requestingUserId)) {
            throw new BusinessRuleViolationException("本日のAI機能の利用回数上限に達しました。明日以降に再度お試しください。",
                    UsageQuotaService.EXCEEDED_ERROR_CODE);
        }
        int recentWindowDays = request.recentWindowDays() == null || request.recentWindowDays() <= 0
                ? DEFAULT_RECENT_WINDOW_DAYS : request.recentWindowDays();
        int baselineWindowDays = request.baselineWindowDays() == null || request.baselineWindowDays() <= 0
                ? DEFAULT_BASELINE_WINDOW_DAYS : request.baselineWindowDays();

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime recentStart = now.minusDays(recentWindowDays);
        OffsetDateTime baselineStart = recentStart.minusDays(baselineWindowDays);

        List<Post> posts = postRepository.search(
                new PostSearchCriteria(null, null, null, request.platform(), 0, MAX_POSTS_FOR_AGGREGATION,
                        "publishedAt", false)).content();

        List<PreprocessedPost> recentPosts = new ArrayList<>();
        List<PreprocessedPost> baselinePosts = new ArrayList<>();
        for (Post post : posts) {
            OffsetDateTime publishedAt = post.getPublishedAt();
            if (publishedAt == null || publishedAt.isBefore(baselineStart)) {
                continue;
            }
            PreprocessedPost preprocessed = postPreprocessor.preprocess(postNormalizer.normalize(post));
            if (!publishedAt.isBefore(recentStart)) {
                recentPosts.add(preprocessed);
            } else {
                baselinePosts.add(preprocessed);
            }
        }

        List<TrendItem> items = new ArrayList<>();
        items.addAll(buildCategoryItems(TrendCategory.HASHTAG, countHashtags(recentPosts), countHashtags(baselinePosts)));
        items.addAll(buildCategoryItems(TrendCategory.GENRE, countGenres(recentPosts), countGenres(baselinePosts)));
        items.addAll(buildCategoryItems(TrendCategory.CONTENT_FORMAT, countContentFormats(recentPosts),
                countContentFormats(baselinePosts)));

        String aiSummary = aiTrendSummaryPort.summarize(items);

        TrendReport report = TrendReport.builder()
                .id(UUID.randomUUID())
                .platform(request.platform())
                .recentWindowDays(recentWindowDays)
                .baselineWindowDays(baselineWindowDays)
                .items(items)
                .aiSummary(aiSummary)
                .createdAt(now)
                .build();

        return TrendReportDto.from(trendReportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public List<TrendReportDto> findLatest(Platform platform, int limit) {
        return trendReportRepository.findLatest(platform, limit).stream()
                .map(TrendReportDto::from)
                .toList();
    }

    private List<TrendItem> buildCategoryItems(TrendCategory category, Map<String, Integer> recentCounts,
                                                Map<String, Integer> baselineCounts) {
        Set<String> allValues = new java.util.HashSet<>(recentCounts.keySet());
        allValues.addAll(baselineCounts.keySet());

        List<TrendItem> candidates = new ArrayList<>();
        for (String value : allValues) {
            int recentCount = recentCounts.getOrDefault(value, 0);
            int baselineCount = baselineCounts.getOrDefault(value, 0);
            if (recentCount < MIN_RECENT_SAMPLE_SIZE) {
                continue;
            }
            if (baselineCount == 0) {
                candidates.add(new TrendItem(category, value, recentCount, 0, null, true));
            } else {
                double growthRatePercent = (recentCount - baselineCount) * 100.0 / baselineCount;
                candidates.add(new TrendItem(category, value, recentCount, baselineCount, growthRatePercent, false));
            }
        }

        candidates.sort(Comparator
                .comparing((TrendItem i) -> i.emerging() ? 1 : 0).reversed()
                .thenComparing(i -> i.emerging() ? i.recentCount() : (i.growthRatePercent() == null ? 0.0 : i.growthRatePercent()),
                        Comparator.reverseOrder()));

        return candidates.size() > TOP_N_PER_CATEGORY ? candidates.subList(0, TOP_N_PER_CATEGORY) : candidates;
    }

    private Map<String, Integer> countHashtags(List<PreprocessedPost> posts) {
        Map<String, Integer> counts = new HashMap<>();
        for (PreprocessedPost post : posts) {
            for (String hashtag : post.hashtags()) {
                counts.merge(hashtag, 1, Integer::sum);
            }
        }
        return counts;
    }

    private Map<String, Integer> countGenres(List<PreprocessedPost> posts) {
        Map<String, Integer> counts = new HashMap<>();
        for (PreprocessedPost post : posts) {
            analysisResultRepository.findByPostId(post.postId())
                    .map(AnalysisResult::getGenre)
                    .filter(genre -> genre != null && !genre.isBlank())
                    .ifPresent(genre -> counts.merge(genre, 1, Integer::sum));
        }
        return counts;
    }

    private Map<String, Integer> countContentFormats(List<PreprocessedPost> posts) {
        Map<String, Integer> counts = new HashMap<>();
        for (PreprocessedPost post : posts) {
            ContentFormat format = post.contentFormat();
            if (format != null) {
                counts.merge(format.name(), 1, Integer::sum);
            }
        }
        return counts;
    }
}
