package com.buzzanalysis.application.evaluation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * 投稿評価APIのリクエストDTO（Phase14）。{@code proposalId}は任意（指定時のみ企画との一致率を算出）。
 * 台本・カルーセル・その他ユーザーの自由入力のいずれも、この構造化フィールドに正規化して渡す。
 */
public record PostEvaluationRequest(UUID proposalId,
                                     @NotBlank @Size(max = 200) String title,
                                     @Size(max = 2000) String hookText,
                                     @Size(max = 2000) String structureText,
                                     @Size(max = 2000) String ctaText,
                                     @Size(max = 2000) String targetAudienceText) {
}
