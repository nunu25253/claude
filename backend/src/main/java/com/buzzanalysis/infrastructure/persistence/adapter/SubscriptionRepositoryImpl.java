package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.billing.Subscription;
import com.buzzanalysis.domain.billing.SubscriptionPlan;
import com.buzzanalysis.domain.billing.SubscriptionRepository;
import com.buzzanalysis.infrastructure.persistence.mapper.SubscriptionMapper;
import com.buzzanalysis.infrastructure.persistence.repository.SubscriptionJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** {@link SubscriptionRepository} のJPA実装（Repositoryパターン）。 */
@Repository
public class SubscriptionRepositoryImpl implements SubscriptionRepository {

    private final SubscriptionJpaRepository jpaRepository;
    private final SubscriptionMapper mapper;

    public SubscriptionRepositoryImpl(SubscriptionJpaRepository jpaRepository, SubscriptionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Subscription save(Subscription subscription) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(subscription)));
    }

    @Override
    public Optional<Subscription> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public List<Subscription> findActiveProSubscriptionsDueForRenewal(OffsetDateTime asOf) {
        return jpaRepository.findByPlanAndCurrentPeriodEndLessThanEqual(SubscriptionPlan.PRO, asOf).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
