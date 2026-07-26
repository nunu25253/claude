package com.buzzanalysis.infrastructure.scheduling;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * トレンド分析の日次自動実行バッチ設定。application.ymlの {@code batch.trend.*} にバインドされる。
 * {@link BatchSyncProperties}と同じ方針で、未設定でも安全に動作するようデフォルトはOFF
 * （{@code enabled=false}）とする。
 */
@ConfigurationProperties(prefix = "batch.trend")
public class BatchTrendProperties {

    /** バッチを有効にするかどうか。falseの場合、スケジューラは登録されない。 */
    private boolean enabled = false;

    /** cron式（デフォルト: 毎日深夜3時）。 */
    private String cron = "0 0 3 * * *";

    /** 直近ウィンドウの日数。 */
    private int recentWindowDays = 7;

    /** ベースラインウィンドウの日数。 */
    private int baselineWindowDays = 21;

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

    public int getRecentWindowDays() {
        return recentWindowDays;
    }

    public void setRecentWindowDays(int recentWindowDays) {
        this.recentWindowDays = recentWindowDays;
    }

    public int getBaselineWindowDays() {
        return baselineWindowDays;
    }

    public void setBaselineWindowDays(int baselineWindowDays) {
        this.baselineWindowDays = baselineWindowDays;
    }
}
