package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.billing.Subscription;
import com.buzzanalysis.infrastructure.persistence.entity.SubscriptionEntity;
import org.springframework.stereotype.Component;

/** {@link Subscription}（ドメイン）と {@link SubscriptionEntity}（JPA）の相互変換を行う。 */
@Component
public class SubscriptionMapper {

    public SubscriptionEntity toEntity(Subscription subscription) {
        return new SubscriptionEntity(subscription.getId(), subscription.getUserId(), subscription.getPlan(),
                subscription.getStatus(), subscription.getPaymentProviderMemberId(),
                subscription.getCurrentPeriodEnd(), subscription.getCreatedAt(), subscription.getUpdatedAt());
    }

    public Subscription toDomain(SubscriptionEntity entity) {
        return new Subscription(entity.getId(), entity.getUserId(), entity.getPlan(), entity.getStatus(),
                entity.getPaymentProviderMemberId(), entity.getCurrentPeriodEnd(), entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
