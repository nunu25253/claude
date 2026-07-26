package com.buzzanalysis.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 認証エンドポイント(ログイン/登録)へのレート制限設定。application.ymlの
 * {@code app.rate-limit.*} にバインドされる。ブルートフォース/クレデンシャルスタッフィング対策。
 */
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private int authMaxAttempts = 10;
    private int authWindowMinutes = 15;

    public int getAuthMaxAttempts() {
        return authMaxAttempts;
    }

    public void setAuthMaxAttempts(int authMaxAttempts) {
        this.authMaxAttempts = authMaxAttempts;
    }

    public int getAuthWindowMinutes() {
        return authWindowMinutes;
    }

    public void setAuthWindowMinutes(int authWindowMinutes) {
        this.authWindowMinutes = authWindowMinutes;
    }
}
