package com.buzzanalysis.application.billing.dto;

/** プラン1件分の表示用情報。{@code monthlyAmountYen}は円単位の月額(FREEは0)。 */
public record PlanInfoDto(String planId, int monthlyAmountYen, int dailyAnalysisLimit) {
}
