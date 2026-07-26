package com.buzzanalysis.infrastructure.payment;

import com.buzzanalysis.infrastructure.security.JwtProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 本番相当の環境でモック決済ゲートウェイ({@code payment.gateway.provider=mock}、既定値)のまま
 * 起動されるのを防ぐfail-fastガード。
 *
 * <p>{@link MockPaymentGatewayAdapter}は外部通信を一切行わず常に決済成功を返す(開発/評価環境向け)。
 * これが本番相当のデプロイに混入すると、任意のカードトークン文字列で無償にPROプランへ
 * アップグレードできてしまい、実収益に直結する重大な事故になる(レビューで発覚)。
 * {@link InsecureSecretGuard}と同じ理由により専用の"prod"プロファイルが存在しないため、
 * {@link JwtProperties#isCookieSecure()}(本番HTTPS環境でのみtrueにする、という既存の規約値)を
 * 「本番相当のデプロイである」というシグナルとして利用する。</p>
 */
@Component
public class MockPaymentGatewayGuard {

    private final JwtProperties jwtProperties;
    private final String paymentGatewayProvider;

    public MockPaymentGatewayGuard(JwtProperties jwtProperties,
                                    @Value("${payment.gateway.provider:mock}") String paymentGatewayProvider) {
        this.jwtProperties = jwtProperties;
        this.paymentGatewayProvider = paymentGatewayProvider;
    }

    @PostConstruct
    void validatePaymentGatewayIsNotMockInProduction() {
        if (!jwtProperties.isCookieSecure()) {
            return;
        }
        if ("mock".equalsIgnoreCase(paymentGatewayProvider)) {
            throw new IllegalStateException(
                    "JWT_COOKIE_SECURE=true (production-like deployment) but PAYMENT_GATEWAY_PROVIDER is still "
                            + "\"mock\" (accepts any card token as a successful payment). Set PAYMENT_GATEWAY_PROVIDER=gmo "
                            + "with real GMO_PAYMENT_SHOP_ID/GMO_PAYMENT_SHOP_PASS before starting in production.");
        }
    }
}
