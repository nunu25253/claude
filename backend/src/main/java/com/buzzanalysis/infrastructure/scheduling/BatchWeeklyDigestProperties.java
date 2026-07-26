package com.buzzanalysis.infrastructure.scheduling;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 週次AIダイジェストメール配信バッチの設定。application.ymlの {@code batch.weekly-digest.*} にバインドされる。
 * {@link BatchAlertProperties}と同じ方針で、蓄積済みデータのみを参照し外部APIへは一切アクセスしない
 * (メール送信のみ)ため気軽に有効化できるが、他のバッチとの一貫性のためデフォルトはOFFとする。
 */
@ConfigurationProperties(prefix = "batch.weekly-digest")
public class BatchWeeklyDigestProperties {

    /** バッチを有効にするかどうか。falseの場合、スケジューラは登録されない。 */
    private boolean enabled = false;

    /** cron式（デフォルト: 毎週月曜9時）。 */
    private String cron = "0 0 9 * * MON";

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
