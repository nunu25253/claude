package com.buzzanalysis.infrastructure.external.platform.x;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** X (Twitter) API v2接続設定。application.ymlの {@code sns.x.*} にバインドされる。 */
@ConfigurationProperties(prefix = "sns.x")
public class XApiProperties {

    private String bearerToken = "";
    private String apiBaseUrl = "https://api.twitter.com/2";

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public boolean isConfigured() {
        return bearerToken != null && !bearerToken.isBlank();
    }
}
