package com.buzzanalysis.infrastructure.scheduling;

import com.buzzanalysis.application.sync.PeriodicSyncApplicationService;
import com.buzzanalysis.application.sync.dto.SyncSummaryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定期データ取得バッチ（自動化）のスケジュール実行を担う。
 * {@code batch.sync.enabled=true} の場合にのみBean登録され、{@code batch.sync.cron} に従って
 * {@link PeriodicSyncApplicationService#syncAll} を呼び出す。
 * デフォルトは無効（false）とし、明示的に有効化した環境でのみ公式APIへ定期アクセスする。
 */
@Component
@ConditionalOnProperty(prefix = "batch.sync", name = "enabled", havingValue = "true")
public class PeriodicSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(PeriodicSyncScheduler.class);

    private final PeriodicSyncApplicationService periodicSyncApplicationService;
    private final BatchSyncProperties properties;

    public PeriodicSyncScheduler(PeriodicSyncApplicationService periodicSyncApplicationService,
                                  BatchSyncProperties properties) {
        this.periodicSyncApplicationService = periodicSyncApplicationService;
        this.properties = properties;
        log.info("Periodic data sync batch enabled. cron='{}'", properties.getCron());
    }

    @Scheduled(cron = "${batch.sync.cron:0 0 * * * *}")
    public void run() {
        log.info("Periodic data sync batch started");
        SyncSummaryDto summary = periodicSyncApplicationService.syncAll(
                properties.getPostLimitPerAccount(),
                properties.getDelayBetweenAccountsMs(),
                properties.getRankingCandidatePoolSize(),
                properties.getRankingTopN());
        log.info("Periodic data sync batch completed: {}", summary);
    }
}
