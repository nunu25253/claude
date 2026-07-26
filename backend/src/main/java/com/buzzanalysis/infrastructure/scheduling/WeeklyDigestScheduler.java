package com.buzzanalysis.infrastructure.scheduling;

import com.buzzanalysis.application.savedanalysis.WeeklyDigestApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 週次AIダイジェストメール配信の定期実行を担う。{@link ThresholdAlertScheduler}と同じパターンで、
 * {@code batch.weekly-digest.enabled=true} の場合にのみBean登録され、
 * {@code batch.weekly-digest.cron} に従って{@link WeeklyDigestApplicationService#sendDigests}を呼び出す。
 * デフォルトは無効（false）。
 */
@Component
@ConditionalOnProperty(prefix = "batch.weekly-digest", name = "enabled", havingValue = "true")
public class WeeklyDigestScheduler {

    private static final Logger log = LoggerFactory.getLogger(WeeklyDigestScheduler.class);

    private final WeeklyDigestApplicationService weeklyDigestApplicationService;
    private final BatchWeeklyDigestProperties properties;

    public WeeklyDigestScheduler(WeeklyDigestApplicationService weeklyDigestApplicationService,
                                  BatchWeeklyDigestProperties properties) {
        this.weeklyDigestApplicationService = weeklyDigestApplicationService;
        this.properties = properties;
        log.info("Weekly digest batch enabled. cron='{}'", properties.getCron());
    }

    @Scheduled(cron = "${batch.weekly-digest.cron:0 0 9 * * MON}")
    public void run() {
        log.info("Weekly digest batch started");
        int sentCount = weeklyDigestApplicationService.sendDigests();
        log.info("Weekly digest batch completed: {} digest(s) sent", sentCount);
    }
}
