package com.buzzanalysis.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * アカウント単位のログイン失敗回数によるロックアウト設定。application.ymlの
 * {@code app.account-lockout.*} にバインドされる。{@link RateLimitFilter}のIPベース制限を
 * 分散(多数のIP/ボットネットを使った)クレデンシャルスタッフィングから補完する。
 */
@ConfigurationProperties(prefix = "app.account-lockout")
public class AccountLockoutProperties {

    private int maxFailedAttempts = 10;
    private int failureWindowMinutes = 15;
    private int lockoutMinutes = 15;

    public int getMaxFailedAttempts() {
        return maxFailedAttempts;
    }

    public void setMaxFailedAttempts(int maxFailedAttempts) {
        this.maxFailedAttempts = maxFailedAttempts;
    }

    public int getFailureWindowMinutes() {
        return failureWindowMinutes;
    }

    public void setFailureWindowMinutes(int failureWindowMinutes) {
        this.failureWindowMinutes = failureWindowMinutes;
    }

    public int getLockoutMinutes() {
        return lockoutMinutes;
    }

    public void setLockoutMinutes(int lockoutMinutes) {
        this.lockoutMinutes = lockoutMinutes;
    }
}
