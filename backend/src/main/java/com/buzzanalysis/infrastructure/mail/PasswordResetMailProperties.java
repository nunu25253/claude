package com.buzzanalysis.infrastructure.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 認証まわりのメール(パスワードリセット・メールアドレス確認)の設定。
 * application.ymlの {@code app.mail.*} にバインドされる。
 */
@ConfigurationProperties(prefix = "app.mail")
public class PasswordResetMailProperties {

    /** フロントエンドのパスワード再設定ページのURL。末尾に {@code ?token=<rawToken>} を付与して使う。 */
    private String resetLinkBaseUrl = "http://localhost:3000/reset-password";

    /** フロントエンドのメールアドレス確認ページのURL。末尾に {@code ?token=<rawToken>} を付与して使う。 */
    private String verificationLinkBaseUrl = "http://localhost:3000/verify-email";

    private String fromAddress = "no-reply@buzz-analysis.local";

    public String getResetLinkBaseUrl() {
        return resetLinkBaseUrl;
    }

    public void setResetLinkBaseUrl(String resetLinkBaseUrl) {
        this.resetLinkBaseUrl = resetLinkBaseUrl;
    }

    public String getVerificationLinkBaseUrl() {
        return verificationLinkBaseUrl;
    }

    public void setVerificationLinkBaseUrl(String verificationLinkBaseUrl) {
        this.verificationLinkBaseUrl = verificationLinkBaseUrl;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }
}
