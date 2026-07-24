package com.buzzanalysis.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

/** {@link TurnstileCaptchaVerificationAdapter} の単体テスト。 */
class TurnstileCaptchaVerificationAdapterTest {

    private static final String VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    private CaptchaProperties properties;
    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        properties = new CaptchaProperties();
        properties.setSecretKey("test-secret");
        properties.setVerifyUrl(VERIFY_URL);

        restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    @Test
    void verify_alwaysSucceeds_whenCaptchaIsDisabled() {
        properties.setEnabled(false);
        TurnstileCaptchaVerificationAdapter adapter = new TurnstileCaptchaVerificationAdapter(properties, restClientBuilder);

        boolean result = adapter.verify(null, "127.0.0.1");

        assertThat(result).isTrue();
        mockServer.verify(); // 呼び出しが一切行われていないことも暗に確認する(期待リクエスト登録無し)
    }

    @Test
    void verify_returnsFalse_whenTokenIsMissing_withoutCallingApi() {
        properties.setEnabled(true);
        TurnstileCaptchaVerificationAdapter adapter = new TurnstileCaptchaVerificationAdapter(properties, restClientBuilder);

        boolean result = adapter.verify("", "127.0.0.1");

        assertThat(result).isFalse();
        mockServer.verify();
    }

    @Test
    void verify_returnsTrue_whenCloudflareRespondsSuccess() {
        properties.setEnabled(true);
        mockServer.expect(requestTo(VERIFY_URL))
                .andExpect(method(POST))
                .andRespond(withSuccess(
                        "{\"success\":true,\"challenge_ts\":\"2026-01-01T00:00:00Z\",\"hostname\":\"example.com\"}",
                        MediaType.APPLICATION_JSON));
        TurnstileCaptchaVerificationAdapter adapter = new TurnstileCaptchaVerificationAdapter(properties, restClientBuilder);

        boolean result = adapter.verify("valid-token", "127.0.0.1");

        assertThat(result).isTrue();
        mockServer.verify();
    }

    @Test
    void verify_returnsFalse_whenCloudflareRespondsFailure() {
        properties.setEnabled(true);
        mockServer.expect(requestTo(VERIFY_URL))
                .andExpect(method(POST))
                .andRespond(withSuccess("{\"success\":false,\"error-codes\":[\"invalid-input-response\"]}",
                        MediaType.APPLICATION_JSON));
        TurnstileCaptchaVerificationAdapter adapter = new TurnstileCaptchaVerificationAdapter(properties, restClientBuilder);

        boolean result = adapter.verify("invalid-token", "127.0.0.1");

        assertThat(result).isFalse();
        mockServer.verify();
    }

    @Test
    void verify_returnsFalse_whenApiCallFails_failClosed() {
        properties.setEnabled(true);
        mockServer.expect(requestTo(VERIFY_URL))
                .andExpect(method(POST))
                .andRespond(withServerError());
        TurnstileCaptchaVerificationAdapter adapter = new TurnstileCaptchaVerificationAdapter(properties, restClientBuilder);

        boolean result = adapter.verify("some-token", "127.0.0.1");

        assertThat(result).isFalse();
        mockServer.verify();
    }
}
