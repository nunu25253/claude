package com.buzzanalysis.infrastructure.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * アプリケーションが送信するメール(パスワードリセット・メールアドレス確認・しきい値アラート等)の
 * リンク先URLや送信元アドレスの設定。application.ymlの {@code app.mail.*} にバインドされる。
 */
@ConfigurationProperties(prefix = "app.mail")
public class PasswordResetMailProperties {

    /** フロントエンドのパスワード再設定ページのURL。末尾に {@code ?token=<rawToken>} を付与して使う。 */
    private String resetLinkBaseUrl = "http://localhost:3000/reset-password";

    /** フロントエンドのメールアドレス確認ページのURL。末尾に {@code ?token=<rawToken>} を付与して使う。 */
    private String verificationLinkBaseUrl = "http://localhost:3000/verify-email";

    /** フロントエンドの保存済み分析一覧ページのURL。しきい値アラートメールのリンク先に使う。 */
    private String savedAnalysesUrl = "http://localhost:3000/saved";

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

    public String getSavedAnalysesUrl() {
        return savedAnalysesUrl;
    }

    public void setSavedAnalysesUrl(String savedAnalysesUrl) {
        this.savedAnalysesUrl = savedAnalysesUrl;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }
}
