package com.buzzanalysis.application.evaluation.dto;

import java.util.UUID;

/**
 * 投稿評価APIのリクエストDTO（Phase14）。{@code proposalId}は任意（指定時のみ企画との一致率を算出）。
 * 台本・カルーセル・その他ユーザーの自由入力のいずれも、この構造化フィールドに正規化して渡す。
 */
public record PostEvaluationRequest(UUID proposalId, String title, String hookText, String structureText,
                                     String ctaText, String targetAudienceText) {
}
