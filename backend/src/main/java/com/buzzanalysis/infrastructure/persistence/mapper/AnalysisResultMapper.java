package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.infrastructure.persistence.entity.AnalysisResultEntity;
import org.springframework.stereotype.Component;

/** {@link AnalysisResult}（ドメイン）と {@link AnalysisResultEntity}（JPA）の相互変換を行う。 */
@Component
public class AnalysisResultMapper {

    public AnalysisResultEntity toEntity(AnalysisResult result) {
        return new AnalysisResultEntity(
                result.getId(), result.getPostId(), result.getGenre(), result.getSubGenre(),
                result.getWhyItWentViral(), result.getTargetAudience(), result.getPostPurpose(), result.getHook(),
                result.getCallToAction(), result.getPostStructureAnalysis(), result.getSentimentAnalysis(),
                result.getVideoStructureAnalysis(), result.getCarouselStructureAnalysis(), result.getTitleAnalysis(),
                result.getTextAnalysis(), result.getPostingTimeAnalysis(), result.getHashtagAnalysis(),
                result.getStrengths(), result.getWeaknesses(), result.getImprovementSuggestions(),
                result.getCreatedAt()
        );
    }

    public AnalysisResult toDomain(AnalysisResultEntity entity) {
        return AnalysisResult.builder()
                .id(entity.getId())
                .postId(entity.getPostId())
                .genre(entity.getGenre())
                .subGenre(entity.getSubGenre())
                .whyItWentViral(entity.getWhyItWentViral())
                .targetAudience(entity.getTargetAudience())
                .postPurpose(entity.getPostPurpose())
                .hook(entity.getHook())
                .callToAction(entity.getCallToAction())
                .postStructureAnalysis(entity.getPostStructureAnalysis())
                .sentimentAnalysis(entity.getSentimentAnalysis())
                .videoStructureAnalysis(entity.getVideoStructureAnalysis())
                .carouselStructureAnalysis(entity.getCarouselStructureAnalysis())
                .titleAnalysis(entity.getTitleAnalysis())
                .textAnalysis(entity.getTextAnalysis())
                .postingTimeAnalysis(entity.getPostingTimeAnalysis())
                .hashtagAnalysis(entity.getHashtagAnalysis())
                .strengths(entity.getStrengths())
                .weaknesses(entity.getWeaknesses())
                .improvementSuggestions(entity.getImprovementSuggestions())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
