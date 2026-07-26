package com.buzzanalysis.application.sync.dto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 定期データ取得バッチ1回分の実行結果サマリー。
 *
 * @param trackedAccountCount 追跡対象として処理したアカウント数
 * @param syncedPostCount     新規作成または更新した投稿数
 * @param updatedRankingCount 再計算して保存したランキングエントリ数
 * @param accountErrors       アカウント単位で発生したエラーメッセージ（他アカウントの処理は継続する）
 * @param startedAt           バッチ開始日時
 * @param finishedAt          バッチ終了日時
 */
public record SyncSummaryDto(
        int trackedAccountCount,
        int syncedPostCount,
        int updatedRankingCount,
        List<String> accountErrors,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt
) {
}
