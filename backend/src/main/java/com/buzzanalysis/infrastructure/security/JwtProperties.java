package com.buzzanalysis.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** JWT関連設定。application.ymlの {@code jwt.*} にバインドされる。 */
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** HMAC署名鍵（Base64不要の生文字列。本番では十分な長さのランダム文字列を環境変数で注入する）。 */
    private String secret = "change-this-secret-in-production-please-0123456789abcdef";
    private String issuer = "buzz-analysis-platform";
    private long accessTokenExpirationMinutes = 30;
    private long refreshTokenExpirationDays = 14;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public long getAccessTokenExpirationMinutes() {
        return accessTokenExpirationMinutes;
    }

    public void setAccessTokenExpirationMinutes(long accessTokenExpirationMinutes) {
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
    }

    public long getRefreshTokenExpirationDays() {
        return refreshTokenExpirationDays;
    }

    public void setRefreshTokenExpirationDays(long refreshTokenExpirationDays) {
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }
}
