package com.buzzanalysis.application.post.dto;

import com.buzzanalysis.domain.score.BuzzScore;

import java.util.Map;
import java.util.UUID;

/** BuzzScore集約のapplication層向けDTO。 */
public record BuzzScoreDto(UUID postId, double totalScore, Map<String, Double> breakdown) {

    public static BuzzScoreDto from(BuzzScore score) {
        return new BuzzScoreDto(score.getPostId(), score.getTotalScore(), score.getBreakdown());
    }
}
