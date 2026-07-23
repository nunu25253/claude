package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.domain.billing.SubscriptionPlan;
import com.buzzanalysis.infrastructure.persistence.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionEntity, UUID> {
    Optional<SubscriptionEntity> findByUserId(UUID userId);

    List<SubscriptionEntity> findByPlanAndCurrentPeriodEndLessThanEqual(SubscriptionPlan plan, OffsetDateTime asOf);
}
