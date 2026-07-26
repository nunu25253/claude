package com.buzzanalysis.infrastructure.payment;

import com.buzzanalysis.domain.billing.PaymentChargeResult;
import com.buzzanalysis.domain.billing.PaymentGatewayPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * {@link PaymentGatewayPort} のモック実装。決済代行事業者と未契約の開発/評価環境向けの既定実装
 * ({@code payment.gateway.provider=gmo} を明示した場合は{@link GmoPaymentGatewayAdapter}に切り替わる)。
 * 実際の外部通信は一切行わず、常に成功として振る舞う。
 */
@Component
@ConditionalOnProperty(prefix = "payment.gateway", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockPaymentGatewayAdapter implements PaymentGatewayPort {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentGatewayAdapter.class);

    @Override
    public PaymentChargeResult registerMemberAndStartRecurringCharge(UUID userId, String email, String cardToken) {
        log.info("[mock-payment] registering member and starting recurring charge for userId={}", userId);
        return PaymentChargeResult.success("mock-member-" + userId);
    }

    @Override
    public void cancelRecurringCharge(String paymentProviderMemberId) {
        log.info("[mock-payment] canceling recurring charge for paymentProviderMemberId={}", paymentProviderMemberId);
    }

    @Override
    public PaymentChargeResult chargeRenewal(String paymentProviderMemberId) {
        log.info("[mock-payment] charging renewal for paymentProviderMemberId={}", paymentProviderMemberId);
        return PaymentChargeResult.success(paymentProviderMemberId);
    }
}
