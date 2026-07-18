package com.buzzanalysis.infrastructure.external.platform.instagram;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Instagram Graph API接続設定。application.ymlの {@code sns.instagram.*} にバインドされる。 */
@ConfigurationProperties(prefix = "sns.instagram")
public class InstagramApiProperties {

    /** Instagram Graph APIのアクセストークン。未設定の場合はスタブデータにフォールバックする。 */
    private String accessToken = "";

    /** Instagram Graph APIのベースURL。 */
    private String graphApiBaseUrl = "https://graph.facebook.com/v19.0";

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getGraphApiBaseUrl() {
        return graphApiBaseUrl;
    }

    public void setGraphApiBaseUrl(String graphApiBaseUrl) {
        this.graphApiBaseUrl = graphApiBaseUrl;
    }

    public boolean isConfigured() {
        return accessToken != null && !accessToken.isBlank();
    }
}
