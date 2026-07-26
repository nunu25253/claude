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
    /** 認証CookieにSecure属性を付けるか。本番(HTTPS)では必ずtrueにする。 */
    private boolean cookieSecure = false;
    /** 認証Cookieに明示するDomain属性。フロントとバックエンドが同一ホスト(ポートのみ違う)の
     * ローカル開発では空のままでよい(ホスト限定Cookieとして動作する)。本番でapp.example.com /
     * api.example.comのようにサブドメインを分ける場合は ".example.com" 等を設定する。 */
    private String cookieDomain = "";
    /** 認証CookieのSameSite属性("Strict"/"Lax"/"None")。フロント/バックエンドが別ドメインの場合は
     * "None"(要Secure=true)にする必要がある。 */
    private String cookieSameSite = "Lax";

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

    public boolean isCookieSecure() {
        return cookieSecure;
    }

    public void setCookieSecure(boolean cookieSecure) {
        this.cookieSecure = cookieSecure;
    }

    public String getCookieDomain() {
        return cookieDomain;
    }

    public void setCookieDomain(String cookieDomain) {
        this.cookieDomain = cookieDomain;
    }

    public String getCookieSameSite() {
        return cookieSameSite;
    }

    public void setCookieSameSite(String cookieSameSite) {
        this.cookieSameSite = cookieSameSite;
    }
}
