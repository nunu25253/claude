package com.buzzanalysis.application.billing;

import com.buzzanalysis.application.billing.dto.SubscriptionDto;
import com.buzzanalysis.domain.billing.PaymentChargeResult;
import com.buzzanalysis.domain.billing.PaymentGatewayPort;
import com.buzzanalysis.domain.billing.Subscription;
import com.buzzanalysis.domain.billing.SubscriptionPlan;
import com.buzzanalysis.domain.billing.SubscriptionRepository;
import com.buzzanalysis.domain.billing.SubscriptionStatus;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.user.Role;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import com.buzzanalysis.infrastructure.payment.GmoPaymentProperties;
import com.buzzanalysis.infrastructure.quota.UsageQuotaProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** {@link SubscriptionApplicationService} の単体テスト。 */
@ExtendWith(MockitoExtension.class)
class SubscriptionApplicationServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private PaymentGatewayPort paymentGatewayPort;
    @Mock
    private UserRepository userRepository;

    private SubscriptionApplicationService service;
    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        UsageQuotaProperties usageQuotaProperties = new UsageQuotaProperties();
        usageQuotaProperties.setDailyOpenAiCallsFree(50);
        usageQuotaProperties.setDailyOpenAiCallsPro(500);
        GmoPaymentProperties gmoPaymentProperties = new GmoPaymentProperties();
        service = new SubscriptionApplicationService(subscriptionRepository, paymentGatewayPort, userRepository,
                usageQuotaProperties, gmoPaymentProperties);
        userId = UUID.randomUUID();
        user = new User(userId, "user@example.com", "hash", "テストユーザー", Role.USER, true,
                OffsetDateTime.now(), OffsetDateTime.now());
    }

    @Test
    void getPlans_returnsAmountsAndLimitsFromConfiguredProperties() {
        var plans = service.getPlans();

        assertThat(plans.free().planId()).isEqualTo("FREE");
        assertThat(plans.free().monthlyAmountYen()).isZero();
        assertThat(plans.free().dailyAnalysisLimit()).isEqualTo(50);
        assertThat(plans.pro().planId()).isEqualTo("PRO");
        assertThat(plans.pro().monthlyAmountYen()).isEqualTo(4980);
        assertThat(plans.pro().dailyAnalysisLimit()).isEqualTo(500);
    }

    @Test
    void getMyPlan_returnsFreeWithoutPersisting_whenNoSubscriptionExists() {
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.empty());

        SubscriptionDto result = service.getMyPlan(userId);

        assertThat(result.plan()).isEqualTo(SubscriptionPlan.FREE);
        assertThat(result.dailyAnalysisLimit()).isEqualTo(50);
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void upgradeToPro_succeeds_whenPaymentGatewaySucceeds() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(paymentGatewayPort.registerMemberAndStartRecurringCharge(eq(userId), eq(user.getEmail()), anyString()))
                .thenReturn(PaymentChargeResult.success("gmo-member-1"));
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionDto result = service.upgradeToPro(userId, "tok_test123");

        assertThat(result.plan()).isEqualTo(SubscriptionPlan.PRO);
        assertThat(result.status()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(result.currentPeriodEnd()).isAfter(OffsetDateTime.now());
        assertThat(result.dailyAnalysisLimit()).isEqualTo(500);
    }

    @Test
    void upgradeToPro_throwsBusinessRuleViolation_whenPaymentGatewayFails() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(paymentGatewayPort.registerMemberAndStartRecurringCharge(eq(userId), eq(user.getEmail()), anyString()))
                .thenReturn(PaymentChargeResult.failure("カードが拒否されました"));

        assertThatThrownBy(() -> service.upgradeToPro(userId, "tok_bad"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("カードが拒否されました");
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void upgradeToPro_throwsBusinessRuleViolation_whenAlreadyPro() {
        Subscription existing = new Subscription(UUID.randomUUID(), userId, SubscriptionPlan.PRO,
                SubscriptionStatus.ACTIVE, "gmo-member-1", OffsetDateTime.now().plusDays(10),
                OffsetDateTime.now(), OffsetDateTime.now());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.upgradeToPro(userId, "tok_test123"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void cancel_downgradesToFree_whenCurrentlyPro() {
        Subscription existing = new Subscription(UUID.randomUUID(), userId, SubscriptionPlan.PRO,
                SubscriptionStatus.ACTIVE, "gmo-member-1", OffsetDateTime.now().plusDays(10),
                OffsetDateTime.now(), OffsetDateTime.now());
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionDto result = service.cancel(userId);

        assertThat(result.plan()).isEqualTo(SubscriptionPlan.FREE);
        assertThat(result.status()).isEqualTo(SubscriptionStatus.CANCELED);
        assertThat(result.currentPeriodEnd()).isNull();
        verify(paymentGatewayPort).cancelRecurringCharge("gmo-member-1");
    }

    @Test
    void cancel_throwsBusinessRuleViolation_whenNotCurrentlyPro() {
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancel(userId))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void renewDueSubscriptions_extendsPeriod_whenChargeSucceeds() {
        Subscription due = new Subscription(UUID.randomUUID(), userId, SubscriptionPlan.PRO,
                SubscriptionStatus.ACTIVE, "gmo-member-1", OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().minusMonths(1), OffsetDateTime.now().minusMonths(1));
        when(subscriptionRepository.findActiveProSubscriptionsDueForRenewal(any())).thenReturn(List.of(due));
        when(paymentGatewayPort.chargeRenewal("gmo-member-1")).thenReturn(PaymentChargeResult.success("gmo-member-1"));
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        int renewedCount = service.renewDueSubscriptions();

        assertThat(renewedCount).isEqualTo(1);
        assertThat(due.getPlan()).isEqualTo(SubscriptionPlan.PRO);
        assertThat(due.getCurrentPeriodEnd()).isAfter(OffsetDateTime.now());
    }

    @Test
    void renewDueSubscriptions_downgradesToFree_whenChargeFails() {
        Subscription due = new Subscription(UUID.randomUUID(), userId, SubscriptionPlan.PRO,
                SubscriptionStatus.ACTIVE, "gmo-member-1", OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().minusMonths(1), OffsetDateTime.now().minusMonths(1));
        when(subscriptionRepository.findActiveProSubscriptionsDueForRenewal(any())).thenReturn(List.of(due));
        when(paymentGatewayPort.chargeRenewal("gmo-member-1")).thenReturn(PaymentChargeResult.failure("カード失効"));
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        int renewedCount = service.renewDueSubscriptions();

        assertThat(renewedCount).isEqualTo(0);
        assertThat(due.getPlan()).isEqualTo(SubscriptionPlan.FREE);
        assertThat(due.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
    }
}
