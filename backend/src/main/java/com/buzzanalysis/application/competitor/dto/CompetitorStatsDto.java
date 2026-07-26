package com.buzzanalysis.application.competitor.dto;

import com.buzzanalysis.domain.competitor.CompetitorStats;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** CompetitorStats集約のapplication層向けDTO。 */
public record CompetitorStatsDto(
        UUID socialAccountId,
        double averageLikeCount,
        double averageCommentCount,
        double postingFrequencyPerWeek,
        Map<Integer, Double> postingTimeDistribution,
        Double averageVideoDurationSeconds,
        double averageCaptionLength,
        List<UUID> topPerformingPostIds,
        Double averageViewCount,
        Map<String, Double> postFormatDistribution,
        Map<String, Double> genreDistribution
) {
    public static CompetitorStatsDto from(CompetitorStats stats) {
        return new CompetitorStatsDto(
                stats.getSocialAccountId(), stats.getAverageLikeCount(), stats.getAverageCommentCount(),
                stats.getPostingFrequencyPerWeek(), stats.getPostingTimeDistribution(),
                stats.getAverageVideoDurationSeconds(), stats.getAverageCaptionLength(),
                stats.getTopPerformingPostIds(), stats.getAverageViewCount(), stats.getPostFormatDistribution(),
                stats.getGenreDistribution()
        );
    }
}
