package com.buzzanalysis.application.post.dto;

import com.buzzanalysis.domain.analysis.AnalysisResult;

import java.util.UUID;

/** AnalysisResult集約のapplication層向けDTO。 */
public record AnalysisResultDto(
        UUID id,
        UUID postId,
        String genre,
        String subGenre,
        String whyItWentViral,
        String targetAudience,
        String postPurpose,
        String hook,
        String callToAction,
        String postStructureAnalysis,
        String sentimentAnalysis,
        String videoStructureAnalysis,
        String carouselStructureAnalysis,
        String titleAnalysis,
        String textAnalysis,
        String postingTimeAnalysis,
        String hashtagAnalysis,
        String strengths,
        String weaknesses,
        String improvementSuggestions
) {
    public static AnalysisResultDto from(AnalysisResult r) {
        return new AnalysisResultDto(
                r.getId(), r.getPostId(), r.getGenre(), r.getSubGenre(), r.getWhyItWentViral(),
                r.getTargetAudience(), r.getPostPurpose(), r.getHook(), r.getCallToAction(),
                r.getPostStructureAnalysis(), r.getSentimentAnalysis(), r.getVideoStructureAnalysis(),
                r.getCarouselStructureAnalysis(), r.getTitleAnalysis(), r.getTextAnalysis(),
                r.getPostingTimeAnalysis(), r.getHashtagAnalysis(), r.getStrengths(), r.getWeaknesses(),
                r.getImprovementSuggestions()
        );
    }
}
