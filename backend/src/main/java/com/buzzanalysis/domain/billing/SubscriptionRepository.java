package com.buzzanalysis.domain.billing;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository {
    Subscription save(Subscription subscription);

    Optional<Subscription> findByUserId(UUID userId);

    /** PROプランで契約更新日(currentPeriodEnd)が既に到来している契約一覧を返す(更新バッチ用)。 */
    List<Subscription> findActiveProSubscriptionsDueForRenewal(OffsetDateTime asOf);
}
