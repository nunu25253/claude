package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.sync.PeriodicSyncApplicationService;
import com.buzzanalysis.application.sync.dto.SyncSummaryDto;
import com.buzzanalysis.infrastructure.scheduling.BatchSyncProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 定期データ取得バッチ（自動化）の手動トリガーAPI。
 * cronスケジュール（{@code batch.sync.enabled=true} の場合に有効）を待たずに、
 * 追跡対象アカウントの投稿同期とランキング再計算を即時実行したい場合に利用する。
 */
@RestController
@RequestMapping("/api/v1/sync")
@Tag(name = "Sync", description = "定期データ取得バッチ（自動化）の手動実行")
public class SyncController {

    private final PeriodicSyncApplicationService periodicSyncApplicationService;
    private final BatchSyncProperties properties;

    public SyncController(PeriodicSyncApplicationService periodicSyncApplicationService, BatchSyncProperties properties) {
        this.periodicSyncApplicationService = periodicSyncApplicationService;
        this.properties = properties;
    }

    @Operation(summary = "定期データ取得バッチの即時実行",
            description = "追跡対象アカウントの最新公開投稿を各SNS公式APIから再取得し、BuzzScoreとランキングを再計算する")
    @PostMapping("/run")
    public ResponseEntity<SyncSummaryDto> runNow() {
        SyncSummaryDto summary = periodicSyncApplicationService.syncAll(
                properties.getPostLimitPerAccount(),
                properties.getDelayBetweenAccountsMs(),
                properties.getRankingCandidatePoolSize(),
                properties.getRankingTopN());
        return ResponseEntity.ok(summary);
    }
}
