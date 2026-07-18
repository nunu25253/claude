package com.buzzanalysis.application.competitor;

import com.buzzanalysis.application.competitor.dto.CompetitorStatsDto;
import com.buzzanalysis.domain.account.SocialAccount;
import com.buzzanalysis.domain.account.SocialAccountRepository;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.competitor.CompetitorStats;
import com.buzzanalysis.domain.competitor.CompetitorStatsRepository;
import com.buzzanalysis.domain.normalization.NormalizedPost;
import com.buzzanalysis.domain.normalization.PostNormalizer;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostSearchCriteria;
import com.buzzanalysis.domain.preprocessing.ContentFormat;
import com.buzzanalysis.domain.preprocessing.PostPreprocessor;
import com.buzzanalysis.domain.preprocessing.PreprocessedPost;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 競合アカウント分析ユースケース。指定アカウントの投稿群から平均いいね数・平均コメント数・投稿頻度・
 * 投稿時間傾向・伸びる投稿ランキング・平均再生数・投稿形式分布・ジャンル分布（Phase9で追加）等を集計する。
 * 結果はRedisキャッシュ（{@code competitorStats}）される。
 */
@Service
public class CompetitorAnalysisApplicationService {

    private static final int MAX_POSTS_FOR_AGGREGATION = 500;

    private final SocialAccountRepository socialAccountRepository;
    private final PostRepository postRepository;
    private final CompetitorStatsRepository competitorStatsRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final PostNormalizer postNormalizer;
    private final PostPreprocessor postPreprocessor;

    public CompetitorAnalysisApplicationService(SocialAccountRepository socialAccountRepository,
                                                 PostRepository postRepository,
                                                 CompetitorStatsRepository competitorStatsRepository,
                                                 AnalysisResultRepository analysisResultRepository,
                                                 PostNormalizer postNormalizer,
                                                 PostPreprocessor postPreprocessor) {
        this.socialAccountRepository = socialAccountRepository;
        this.postRepository = postRepository;
        this.competitorStatsRepository = competitorStatsRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.postNormalizer = postNormalizer;
        this.postPreprocessor = postPreprocessor;
    }

    @Cacheable(value = "competitorStats", key = "#accountId")
    @Transactional(readOnly = true)
    public CompetitorStatsDto getStats(UUID accountId) {
        SocialAccount account = socialAccountRepository.findById(accountId)
                .orElseThrow(() -> EntityNotFoundException.of("SocialAccount", accountId));

        List<Post> posts = postRepository.search(
                new PostSearchCriteria(null, null, account.getId(), null, 0, MAX_POSTS_FOR_AGGREGATION, "publishedAt", false)
        ).content();

        CompetitorStats stats = aggregate(account.getId(), posts);
        competitorStatsRepository.save(stats);
        return CompetitorStatsDto.from(stats);
    }

    private CompetitorStats aggregate(UUID accountId, List<Post> posts) {
        if (posts.isEmpty()) {
            return CompetitorStats.calculate(accountId, 0, 0, 0, Map.of(), null, 0, List.of(), null, Map.of(), Map.of());
        }

        double avgLikes = posts.stream().mapToLong(p -> nvl(p.getLikeCount())).average().orElse(0);
        double avgComments = posts.stream().mapToLong(p -> nvl(p.getCommentCount())).average().orElse(0);
        double avgCaptionLength = posts.stream()
                .mapToInt(p -> p.getCaption() == null ? 0 : p.getCaption().length())
                .average().orElse(0);

        List<Post> withVideo = posts.stream().filter(p -> p.getVideoDurationSeconds() != null).toList();
        Double avgVideoDuration = withVideo.isEmpty() ? null :
                withVideo.stream().mapToInt(Post::getVideoDurationSeconds).average().orElse(0);

        double postingFrequencyPerWeek = computePostingFrequencyPerWeek(posts);
        Map<Integer, Double> timeDistribution = computeTimeDistribution(posts);

        List<UUID> topPerforming = posts.stream()
                .sorted(Comparator.comparingLong((Post p) -> nvl(p.getLikeCount()) + nvl(p.getCommentCount())).reversed())
                .limit(10)
                .map(Post::getId)
                .toList();

        List<PreprocessedPost> preprocessedPosts = posts.stream()
                .map(post -> postPreprocessor.preprocess(postNormalizer.normalize(post)))
                .toList();
        List<NormalizedPost> normalizedPosts = posts.stream().map(postNormalizer::normalize).toList();

        Double avgViewCount = computeAverageViewCount(normalizedPosts);
        Map<String, Double> postFormatDistribution = computePostFormatDistribution(preprocessedPosts);
        Map<String, Double> genreDistribution = computeGenreDistribution(posts);

        return CompetitorStats.calculate(accountId, avgLikes, avgComments, postingFrequencyPerWeek,
                timeDistribution, avgVideoDuration, avgCaptionLength, topPerforming, avgViewCount,
                postFormatDistribution, genreDistribution);
    }

    /** 再生数が未計測(null)の投稿は除外して平均を算出する（Phase1の教訓: 未計測と0を混同しない）。 */
    private Double computeAverageViewCount(List<NormalizedPost> normalizedPosts) {
        List<Long> measuredViews = normalizedPosts.stream()
                .map(NormalizedPost::viewCount)
                .filter(v -> v != null)
                .toList();
        if (measuredViews.isEmpty()) {
            return null;
        }
        return measuredViews.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    private Map<String, Double> computePostFormatDistribution(List<PreprocessedPost> preprocessedPosts) {
        Map<ContentFormat, Long> counts = preprocessedPosts.stream()
                .map(PreprocessedPost::contentFormat)
                .filter(f -> f != null)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return toPercentageMap(counts, preprocessedPosts.size());
    }

    /** ジャンルはPhase5のAnalysisResultが存在する投稿のみ対象とする（未分析投稿は集計から除外）。 */
    private Map<String, Double> computeGenreDistribution(List<Post> posts) {
        Map<String, Long> counts = posts.stream()
                .map(post -> analysisResultRepository.findByPostId(post.getId()).map(ar -> ar.getGenre()).orElse(null))
                .filter(genre -> genre != null && !genre.isBlank())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        return toPercentageMap(counts, total);
    }

    private <K> Map<String, Double> toPercentageMap(Map<K, Long> counts, long total) {
        if (total == 0) {
            return Map.of();
        }
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<K, Long> entry : counts.entrySet()) {
            result.put(String.valueOf(entry.getKey()), (entry.getValue() * 100.0) / total);
        }
        return result;
    }

    private double computePostingFrequencyPerWeek(List<Post> posts) {
        OffsetDateTime earliest = posts.stream().map(Post::getPublishedAt).filter(java.util.Objects::nonNull)
                .min(Comparator.naturalOrder()).orElse(null);
        OffsetDateTime latest = posts.stream().map(Post::getPublishedAt).filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);
        if (earliest == null || latest == null || earliest.equals(latest)) {
            return posts.size();
        }
        long days = Math.max(1, ChronoUnit.DAYS.between(earliest, latest));
        double weeks = Math.max(1.0 / 7.0, days / 7.0);
        return Math.round((posts.size() / weeks) * 100.0) / 100.0;
    }

    private Map<Integer, Double> computeTimeDistribution(List<Post> posts) {
        Map<Integer, Long> hourCounts = new HashMap<>();
        long withTimestamp = 0;
        for (Post post : posts) {
            if (post.getPublishedAt() == null) {
                continue;
            }
            int hour = post.getPublishedAt().getHour();
            hourCounts.merge(hour, 1L, Long::sum);
            withTimestamp++;
        }
        Map<Integer, Double> distribution = new HashMap<>();
        for (Map.Entry<Integer, Long> entry : hourCounts.entrySet()) {
            distribution.put(entry.getKey(), withTimestamp == 0 ? 0.0 : (entry.getValue() * 100.0) / withTimestamp);
        }
        return distribution;
    }

    private long nvl(Long value) {
        return value == null ? 0L : value;
    }
}
