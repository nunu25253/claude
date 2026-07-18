package com.buzzanalysis.infrastructure.external.platform.tiktok;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** TikTok Display API接続設定。application.ymlの {@code sns.tiktok.*} にバインドされる。 */
@ConfigurationProperties(prefix = "sns.tiktok")
public class TikTokApiProperties {

    private String accessToken = "";
    private String apiBaseUrl = "https://open.tiktokapis.com/v2";

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public boolean isConfigured() {
        return accessToken != null && !accessToken.isBlank();
    }
}
