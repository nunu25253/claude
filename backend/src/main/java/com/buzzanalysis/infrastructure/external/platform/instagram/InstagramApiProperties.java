package com.buzzanalysis.infrastructure.external.platform.instagram;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Instagram Graph API接続設定。application.ymlの {@code sns.instagram.*} にバインドされる。 */
@ConfigurationProperties(prefix = "sns.instagram")
public class InstagramApiProperties {

    /** Instagram Graph APIのアクセストークン（Business Discovery呼び出し元となる自社アカウントのトークン）。 */
    private String accessToken = "";

    /**
     * Business Discovery呼び出しの起点となる、自社のInstagramビジネス/クリエイターアカウントのIG User ID。
     * Business Discovery APIは「自社アカウントのトークンを使って“他の”ビジネス/クリエイターアカウントの
     * 公開データを閲覧する」仕組みのため、accessTokenだけでなくこのIDも必須。
     */
    private String businessAccountId = "";

    /** Instagram Graph APIのベースURL。 */
    private String graphApiBaseUrl = "https://graph.facebook.com/v19.0";

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getBusinessAccountId() {
        return businessAccountId;
    }

    public void setBusinessAccountId(String businessAccountId) {
        this.businessAccountId = businessAccountId;
    }

    public String getGraphApiBaseUrl() {
        return graphApiBaseUrl;
    }

    public void setGraphApiBaseUrl(String graphApiBaseUrl) {
        this.graphApiBaseUrl = graphApiBaseUrl;
    }

    /** Business Discovery呼び出し（アカウント情報/最新投稿一覧の取得）に必要な設定が揃っているか。 */
    public boolean isConfigured() {
        return accessToken != null && !accessToken.isBlank()
                && businessAccountId != null && !businessAccountId.isBlank();
    }
}
