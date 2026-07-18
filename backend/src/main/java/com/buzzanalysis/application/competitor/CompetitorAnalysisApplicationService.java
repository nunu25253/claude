package com.buzzanalysis.application.competitor;

import com.buzzanalysis.application.competitor.dto.CompetitorStatsDto;
import com.buzzanalysis.domain.account.SocialAccount;
import com.buzzanalysis.domain.account.SocialAccountRepository;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.competitor.CompetitorStats;
import com.buzzanalysis.domain.competitor.CompetitorStatsRepository;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostSearchCriteria;
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

/**
 * 競合アカウント分析ユースケース。指定アカウントの投稿群から平均いいね数・平均コメント数・投稿頻度・
 * 投稿時間傾向・伸びる投稿ランキング等を集計する。結果はRedisキャッシュ（{@code competitorStats}）される。
 */
@Service
public class CompetitorAnalysisApplicationService {

    private static final int MAX_POSTS_FOR_AGGREGATION = 500;

    private final SocialAccountRepository socialAccountRepository;
    private final PostRepository postRepository;
    private final CompetitorStatsRepository competitorStatsRepository;

    public CompetitorAnalysisApplicationService(SocialAccountRepository socialAccountRepository,
                                                 PostRepository postRepository,
                                                 CompetitorStatsRepository competitorStatsRepository) {
        this.socialAccountRepository = socialAccountRepository;
        this.postRepository = postRepository;
        this.competitorStatsRepository = competitorStatsRepository;
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
            return CompetitorStats.calculate(accountId, 0, 0, 0, Map.of(), null, 0, List.of());
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

        return CompetitorStats.calculate(accountId, avgLikes, avgComments, postingFrequencyPerWeek,
                timeDistribution, avgVideoDuration, avgCaptionLength, topPerforming);
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
