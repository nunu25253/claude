package com.buzzanalysis.infrastructure.scheduling;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * BuzzScoreしきい値アラート通知バッチの設定。application.ymlの {@code batch.alert.*} にバインドされる。
 * {@link BatchTrendProperties}と同じ方針で、蓄積済みデータのみを参照し外部APIへは一切アクセスしない
 * (メール送信のみ)ため気軽に有効化できるが、他のバッチとの一貫性のためデフォルトはOFFとする。
 */
@ConfigurationProperties(prefix = "batch.alert")
public class BatchAlertProperties {

    /** バッチを有効にするかどうか。falseの場合、スケジューラは登録されない。 */
    private boolean enabled = false;

    /** cron式（デフォルト: 1時間ごと）。 */
    private String cron = "0 0 * * * *";

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
