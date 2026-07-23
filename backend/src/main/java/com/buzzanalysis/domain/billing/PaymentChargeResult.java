package com.buzzanalysis.domain.billing;

/** {@link PaymentGatewayPort#registerMemberAndStartRecurringCharge} の結果。 */
public record PaymentChargeResult(boolean success, String paymentProviderMemberId, String errorMessage) {

    public static PaymentChargeResult success(String paymentProviderMemberId) {
        return new PaymentChargeResult(true, paymentProviderMemberId, null);
    }

    public static PaymentChargeResult failure(String errorMessage) {
        return new PaymentChargeResult(false, null, errorMessage);
    }
}
