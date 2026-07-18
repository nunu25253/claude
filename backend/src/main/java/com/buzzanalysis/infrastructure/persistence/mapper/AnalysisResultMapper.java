package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.infrastructure.persistence.entity.AnalysisResultEntity;
import org.springframework.stereotype.Component;

/** {@link AnalysisResult}（ドメイン）と {@link AnalysisResultEntity}（JPA）の相互変換を行う。 */
@Component
public class AnalysisResultMapper {

    public AnalysisResultEntity toEntity(AnalysisResult result) {
        return new AnalysisResultEntity(
                result.getId(), result.getPostId(), result.getWhyItWentViral(), result.getTargetAudience(),
                result.getHook(), result.getCallToAction(), result.getSentimentAnalysis(),
                result.getVideoStructureAnalysis(), result.getCarouselStructureAnalysis(), result.getTitleAnalysis(),
                result.getTextAnalysis(), result.getPostingTimeAnalysis(), result.getHashtagAnalysis(),
                result.getImprovementSuggestions(), result.getCreatedAt()
        );
    }

    public AnalysisResult toDomain(AnalysisResultEntity entity) {
        return AnalysisResult.builder()
                .id(entity.getId())
                .postId(entity.getPostId())
                .whyItWentViral(entity.getWhyItWentViral())
                .targetAudience(entity.getTargetAudience())
                .hook(entity.getHook())
                .callToAction(entity.getCallToAction())
                .sentimentAnalysis(entity.getSentimentAnalysis())
                .videoStructureAnalysis(entity.getVideoStructureAnalysis())
                .carouselStructureAnalysis(entity.getCarouselStructureAnalysis())
                .titleAnalysis(entity.getTitleAnalysis())
                .textAnalysis(entity.getTextAnalysis())
                .postingTimeAnalysis(entity.getPostingTimeAnalysis())
                .hashtagAnalysis(entity.getHashtagAnalysis())
                .improvementSuggestions(entity.getImprovementSuggestions())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
