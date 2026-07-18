package com.buzzanalysis.infrastructure.scheduling;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 定期データ取得バッチ（自動同期）の設定。application.ymlの {@code batch.sync.*} にバインドされる。
 * 未設定でも安全に動作するようデフォルトはOFF（{@code enabled=false}）とし、
 * 明示的に有効化した環境でのみ公式APIへの定期アクセスを行う。
 */
@ConfigurationProperties(prefix = "batch.sync")
public class BatchSyncProperties {

    /** バッチを有効にするかどうか。falseの場合、スケジューラは登録されない。 */
    private boolean enabled = false;

    /** cron式（デフォルト: 毎時0分）。 */
    private String cron = "0 0 * * * *";

    /** 1アカウントあたりに取得する最新投稿の最大件数。 */
    private int postLimitPerAccount = 20;

    /** 各SNS公式APIのレート制限に配慮し、アカウント処理間に挟む待機時間（ミリ秒）。 */
    private long delayBetweenAccountsMs = 500;

    /** ランキング再計算で対象とする直近投稿の取得上限件数（プラットフォームごと）。 */
    private int rankingCandidatePoolSize = 200;

    /** 各ランキング種別で上位何件まで保存するか。 */
    private int rankingTopN = 20;

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

    public int getPostLimitPerAccount() {
        return postLimitPerAccount;
    }

    public void setPostLimitPerAccount(int postLimitPerAccount) {
        this.postLimitPerAccount = postLimitPerAccount;
    }

    public long getDelayBetweenAccountsMs() {
        return delayBetweenAccountsMs;
    }

    public void setDelayBetweenAccountsMs(long delayBetweenAccountsMs) {
        this.delayBetweenAccountsMs = delayBetweenAccountsMs;
    }

    public int getRankingCandidatePoolSize() {
        return rankingCandidatePoolSize;
    }

    public void setRankingCandidatePoolSize(int rankingCandidatePoolSize) {
        this.rankingCandidatePoolSize = rankingCandidatePoolSize;
    }

    public int getRankingTopN() {
        return rankingTopN;
    }

    public void setRankingTopN(int rankingTopN) {
        this.rankingTopN = rankingTopN;
    }
}
