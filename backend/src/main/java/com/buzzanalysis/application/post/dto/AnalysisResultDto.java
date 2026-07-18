package com.buzzanalysis.application.post.dto;

import com.buzzanalysis.domain.analysis.AnalysisResult;

import java.util.UUID;

/** AnalysisResult集約のapplication層向けDTO。 */
public record AnalysisResultDto(
        UUID id,
        UUID postId,
        String whyItWentViral,
        String targetAudience,
        String hook,
        String callToAction,
        String sentimentAnalysis,
        String videoStructureAnalysis,
        String carouselStructureAnalysis,
        String titleAnalysis,
        String textAnalysis,
        String postingTimeAnalysis,
        String hashtagAnalysis,
        String improvementSuggestions
) {
    public static AnalysisResultDto from(AnalysisResult r) {
        return new AnalysisResultDto(
                r.getId(), r.getPostId(), r.getWhyItWentViral(), r.getTargetAudience(), r.getHook(),
                r.getCallToAction(), r.getSentimentAnalysis(), r.getVideoStructureAnalysis(),
                r.getCarouselStructureAnalysis(), r.getTitleAnalysis(), r.getTextAnalysis(),
                r.getPostingTimeAnalysis(), r.getHashtagAnalysis(), r.getImprovementSuggestions()
        );
    }
}
