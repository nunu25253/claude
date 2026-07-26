package com.buzzanalysis.infrastructure.persistence.entity;

import com.buzzanalysis.domain.billing.SubscriptionPlan;
import com.buzzanalysis.domain.billing.SubscriptionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/** {@code subscriptions} テーブルに対応するJPAエンティティ。 */
@Entity
@Table(name = "subscriptions")
public class SubscriptionEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status;

    @Column(name = "payment_provider_member_id")
    private String paymentProviderMemberId;

    @Column(name = "current_period_end")
    private OffsetDateTime currentPeriodEnd;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected SubscriptionEntity() {
    }

    public SubscriptionEntity(UUID id, UUID userId, SubscriptionPlan plan, SubscriptionStatus status,
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
