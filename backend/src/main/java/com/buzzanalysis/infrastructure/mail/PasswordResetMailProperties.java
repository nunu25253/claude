package com.buzzanalysis.infrastructure.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * パスワードリセットメールの設定。application.ymlの {@code app.mail.*} にバインドされる。
 */
@ConfigurationProperties(prefix = "app.mail")
public class PasswordResetMailProperties {

    /** フロントエンドのパスワード再設定ページのURL。末尾に {@code ?token=<rawToken>} を付与して使う。 */
    private String resetLinkBaseUrl = "http://localhost:3000/reset-password";

    private String fromAddress = "no-reply@buzz-analysis.local";

    public String getResetLinkBaseUrl() {
        return resetLinkBaseUrl;
    }

    public void setResetLinkBaseUrl(String resetLinkBaseUrl) {
        this.resetLinkBaseUrl = resetLinkBaseUrl;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }
}
