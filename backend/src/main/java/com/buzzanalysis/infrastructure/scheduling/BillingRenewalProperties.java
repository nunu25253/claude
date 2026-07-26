package com.buzzanalysis.infrastructure.scheduling;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * PRO契約の月次更新課金バッチの設定。application.ymlの {@code batch.billing-renewal.*} にバインドされる。
 * 他のバッチと同じ方針でデフォルトはOFFとする(決済処理を伴うため、有効化は運用者の明示判断に委ねる)。
 */
@ConfigurationProperties(prefix = "batch.billing-renewal")
public class BillingRenewalProperties {

    private boolean enabled = false;

    /** cron式(デフォルト: 毎日午前3時)。 */
    private String cron = "0 0 3 * * *";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }
}
