package com.buzzanalysis.domain.billing;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * ユーザー単位の課金プラン契約状況を表す集約(改善計画No.11: 無料枠/有料枠)。
 * ユーザーが一度もアップグレードしていない場合はレコード自体が存在せず、
 * {@link com.buzzanalysis.application.billing.SubscriptionApplicationService} 側でFREE相当として扱う。
 */
public class Subscription {

    private final UUID id;
    private final UUID userId;
    private SubscriptionPlan plan;
    private SubscriptionStatus status;
    /** 決済代行事業者(GMOペイメント等)側の会員ID。PROプランへ移行した後にのみ設定される。 */
    private String paymentProviderMemberId;
    /** 現在の課金期間の終了日時。PROプラン中のみ設定される。 */
    private OffsetDateTime currentPeriodEnd;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Subscription(UUID id, UUID userId, SubscriptionPlan plan, SubscriptionStatus status,
                         String paymentProviderMemberId, OffsetDateTime currentPeriodEnd,
                         OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.plan = plan;
        this.status = status;
        this.paymentProviderMemberId = paymentProviderMemberId;
        this.currentPeriodEnd = currentPeriodEnd;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Subscription createFree(UUID userId) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Subscription(UUID.randomUUID(), userId, SubscriptionPlan.FREE, SubscriptionStatus.ACTIVE,
                null, null, now, now);
    }

    public void upgradeToPro(String paymentProviderMemberId, OffsetDateTime currentPeriodEnd) {
        this.plan = SubscriptionPlan.PRO;
        this.status = SubscriptionStatus.ACTIVE;
        this.paymentProviderMemberId = paymentProviderMemberId;
        this.currentPeriodEnd = currentPeriodEnd;
        this.updatedAt = OffsetDateTime.now();
    }

    /** 継続課金を解約し、即時にFREEプランへ戻す(次回請求分からの解約反映のような猶予期間は設けない簡易実装)。 */
    public void cancel() {
        this.plan = SubscriptionPlan.FREE;
        this.status = SubscriptionStatus.CANCELED;
        this.currentPeriodEnd = null;
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public String getPaymentProviderMemberId() {
        return paymentProviderMemberId;
    }

    public OffsetDateTime getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
