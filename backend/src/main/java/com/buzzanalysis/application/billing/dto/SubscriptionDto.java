package com.buzzanalysis.application.billing.dto;

import com.buzzanalysis.domain.billing.SubscriptionPlan;
import com.buzzanalysis.domain.billing.SubscriptionStatus;

import java.time.OffsetDateTime;

public record SubscriptionDto(SubscriptionPlan plan, SubscriptionStatus status, OffsetDateTime currentPeriodEnd,
                               int dailyAnalysisLimit) {
}
