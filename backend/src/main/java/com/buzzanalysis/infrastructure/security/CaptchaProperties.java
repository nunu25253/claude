package com.buzzanalysis.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cloudflare Turnstile(CAPTCHA)の設定。{@code app.captcha.*}にバインドされる。
 * {@code enabled=false}(既定。ローカル開発・CI用)の場合、検証は常に成功として扱われる。
 */
@ConfigurationProperties(prefix = "app.captcha")
public class CaptchaProperties {

    private boolean enabled = false;
    private String secretKey = "";
    private String verifyUrl = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getVerifyUrl() {
        return verifyUrl;
    }

    public void setVerifyUrl(String verifyUrl) {
        this.verifyUrl = verifyUrl;
    }
}
