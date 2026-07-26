package com.buzzanalysis.infrastructure.security;

import com.buzzanalysis.application.auth.CaptchaVerificationPort;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Cloudflare Turnstileのsiteverify APIを呼び出してCAPTCHAトークンを検証するアダプタ。
 * {@link CaptchaProperties#isEnabled()}がfalse(既定)の場合はAPI呼び出し自体を行わず常に成功を返す
 * (ローカル開発・CI・Turnstileアカウント未設定環境でも登録フローを止めないため)。
 * API呼び出し自体が失敗した場合はfail-closed(検証失敗として扱う)とする。CAPTCHAはBot対策という
 * セキュリティ機能であり、検証できない状態を「素通り」させるとその間Bot対策が無いのと同じになるため、
 * 可用性より安全側に倒す(Cloudflare障害時は{@code app.captcha.enabled=false}へ切り替えて運用回避する)。
 */
@Component
public class TurnstileCaptchaVerificationAdapter implements CaptchaVerificationPort {

    private static final Logger log = LoggerFactory.getLogger(TurnstileCaptchaVerificationAdapter.class);

    private final CaptchaProperties properties;
    private final RestClient restClient;

    public TurnstileCaptchaVerificationAdapter(CaptchaProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public boolean verify(String captchaToken, String remoteIp) {
        if (!properties.isEnabled()) {
            return true;
        }
        if (captchaToken == null || captchaToken.isBlank()) {
            return false;
        }

        StringBuilder form = new StringBuilder()
                .append("secret=").append(encode(properties.getSecretKey()))
                .append("&response=").append(encode(captchaToken));
        if (remoteIp != null && !remoteIp.isBlank()) {
            form.append("&remoteip=").append(encode(remoteIp));
        }

        try {
            TurnstileResponse response = restClient.post()
                    .uri(properties.getVerifyUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form.toString())
                    .retrieve()
                    .body(TurnstileResponse.class);
            return response != null && response.success();
        } catch (RestClientException e) {
            log.warn("Turnstile verification call failed; treating as verification failure (fail-closed)", e);
            return false;
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    /**
     * Turnstile siteverifyレスポンスの必要最小限のフィールドのみをマッピングする。
     * 実際のレスポンスには{@code error-codes}/{@code challenge_ts}/{@code hostname}等も含まれるため
     * 未知プロパティは無視する。
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TurnstileResponse(boolean success) {
    }
}
