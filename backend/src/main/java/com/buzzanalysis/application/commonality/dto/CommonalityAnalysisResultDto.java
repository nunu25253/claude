package com.buzzanalysis.application.commonality.dto;

import com.buzzanalysis.domain.commonality.CommonalityAnalysisResult;
import com.buzzanalysis.domain.preprocessing.ContentFormat;

import java.util.List;

/** {@link CommonalityAnalysisResult}（ドメイン）のapplication層向けDTO。 */
public record CommonalityAnalysisResultDto(
        int totalPostCount,
        int aiSampleSize,
        List<String> commonHashtags,
        Integer commonVideoDurationSeconds,
        Integer commonPostingHour,
        ContentFormat commonContentFormat,
        String commonTitlePattern,
        String commonHookPattern,
        String commonCtaPattern,
        String commonStructurePattern,
        String commonTargetPattern
) {
    public static CommonalityAnalysisResultDto from(CommonalityAnalysisResult r) {
        return new CommonalityAnalysisResultDto(
                r.getTotalPostCount(), r.getAiSampleSize(), r.getCommonHashtags(), r.getCommonVideoDurationSeconds(),
                r.getCommonPostingHour(), r.getCommonContentFormat(), r.getCommonTitlePattern(),
                r.getCommonHookPattern(), r.getCommonCtaPattern(), r.getCommonStructurePattern(),
                r.getCommonTargetPattern()
        );
    }
}
