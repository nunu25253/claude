package com.buzzanalysis.application.savedanalysis.dto;

import java.util.UUID;

/** 分析結果の保存ユースケースの入力コマンド。 */
public record SaveAnalysisCommand(UUID userId, UUID postId, String note) {
}
