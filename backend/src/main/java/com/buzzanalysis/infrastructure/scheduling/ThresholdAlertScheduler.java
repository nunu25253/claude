package com.buzzanalysis.infrastructure.scheduling;

import com.buzzanalysis.application.savedanalysis.ThresholdAlertApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * BuzzScoreしきい値アラート通知の定期実行を担う。{@link TrendAnalysisScheduler}と同じパターンで、
 * {@code batch.alert.enabled=true} の場合にのみBean登録され、{@code batch.alert.cron} に従って
 * {@link ThresholdAlertApplicationService#checkAndSendAlerts} を呼び出す。デフォルトは無効（false）。
 */
@Component
@ConditionalOnProperty(prefix = "batch.alert", name = "enabled", havingValue = "true")
public class ThresholdAlertScheduler {

    private static final Logger log = LoggerFactory.getLogger(ThresholdAlertScheduler.class);

    private final ThresholdAlertApplicationService thresholdAlertApplicationService;
    private final BatchAlertProperties properties;

    public ThresholdAlertScheduler(ThresholdAlertApplicationService thresholdAlertApplicationService,
                                    BatchAlertProperties properties) {
        this.thresholdAlertApplicationService = thresholdAlertApplicationService;
        this.properties = properties;
        log.info("Threshold alert batch enabled. cron='{}'", properties.getCron());
    }

    @Scheduled(cron = "${batch.alert.cron:0 0 * * * *}")
    public void run() {
        log.info("Threshold alert batch started");
        int triggeredCount = thresholdAlertApplicationService.checkAndSendAlerts();
        log.info("Threshold alert batch completed: {} alert(s) sent", triggeredCount);
    }
}
