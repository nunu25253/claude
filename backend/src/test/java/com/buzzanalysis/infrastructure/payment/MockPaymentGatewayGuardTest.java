package com.buzzanalysis.infrastructure.payment;

import com.buzzanalysis.infrastructure.security.JwtProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link MockPaymentGatewayGuard} の単体テスト。
 * cookieSecure=false(開発環境)ではmockプロバイダのままでも起動を許し、
 * cookieSecure=true(本番相当)ではmockプロバイダを拒否することを検証する。
 */
class MockPaymentGatewayGuardTest {

    @Test
    void allowsMockProvider_whenCookieSecureIsFalse() {
        JwtProperties jwtProperties = new JwtProperties();
        MockPaymentGatewayGuard guard = new MockPaymentGatewayGuard(jwtProperties, "mock");

        assertThatCode(guard::validatePaymentGatewayIsNotMockInProduction).doesNotThrowAnyException();
    }

    @Test
    void throws_whenCookieSecureIsTrue_andProviderIsStillMock() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setCookieSecure(true);
        MockPaymentGatewayGuard guard = new MockPaymentGatewayGuard(jwtProperties, "mock");

        assertThatThrownBy(guard::validatePaymentGatewayIsNotMockInProduction)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PAYMENT_GATEWAY_PROVIDER");
    }

    @Test
    void allowsStartup_whenCookieSecureIsTrue_andProviderIsGmo() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setCookieSecure(true);
        MockPaymentGatewayGuard guard = new MockPaymentGatewayGuard(jwtProperties, "gmo");

        assertThatCode(guard::validatePaymentGatewayIsNotMockInProduction).doesNotThrowAnyException();
    }

    @Test
    void providerCheckIsCaseInsensitive() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setCookieSecure(true);
        MockPaymentGatewayGuard guard = new MockPaymentGatewayGuard(jwtProperties, "MOCK");

        assertThatThrownBy(guard::validatePaymentGatewayIsNotMockInProduction)
                .isInstanceOf(IllegalStateException.class);
    }
}
