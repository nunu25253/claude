package com.buzzanalysis.application.organization.dto;

import com.buzzanalysis.application.savedanalysis.dto.SavedAnalysisDetailDto;

import java.util.UUID;

/** チーム内で共有される保存済み分析1件分。誰が保存したかを併せて返す。 */
public record TeamSavedAnalysisDto(
        SavedAnalysisDetailDto savedAnalysis,
        UUID savedByUserId,
        String savedByDisplayName
) {
}
