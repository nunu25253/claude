package com.buzzanalysis.infrastructure.scheduling;

import com.buzzanalysis.application.billing.SubscriptionApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * PRO契約の月次更新課金の定期実行を担う。{@link ThresholdAlertScheduler}と同じパターンで、
 * {@code batch.billing-renewal.enabled=true} の場合にのみBean登録され、
 * {@code batch.billing-renewal.cron} に従って{@link SubscriptionApplicationService#renewDueSubscriptions}を呼び出す。
 * デフォルトは無効（false）。
 */
@Component
@ConditionalOnProperty(prefix = "batch.billing-renewal", name = "enabled", havingValue = "true")
public class BillingRenewalScheduler {

    private static final Logger log = LoggerFactory.getLogger(BillingRenewalScheduler.class);

    private final SubscriptionApplicationService subscriptionApplicationService;
    private final BillingRenewalProperties properties;

    public BillingRenewalScheduler(SubscriptionApplicationService subscriptionApplicationService,
                                    BillingRenewalProperties properties) {
        this.subscriptionApplicationService = subscriptionApplicationService;
        this.properties = properties;
        log.info("Billing renewal batch enabled. cron='{}'", properties.getCron());
    }

    @Scheduled(cron = "${batch.billing-renewal.cron:0 0 3 * * *}")
    public void run() {
        log.info("Billing renewal batch started");
        int renewedCount = subscriptionApplicationService.renewDueSubscriptions();
        log.info("Billing renewal batch completed: {} subscription(s) renewed", renewedCount);
    }
}
