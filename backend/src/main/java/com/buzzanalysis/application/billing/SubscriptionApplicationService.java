package com.buzzanalysis.application.billing;

import com.buzzanalysis.application.billing.dto.BillingPlansDto;
import com.buzzanalysis.application.billing.dto.PlanInfoDto;
import com.buzzanalysis.application.billing.dto.SubscriptionDto;
import com.buzzanalysis.domain.billing.PaymentChargeResult;
import com.buzzanalysis.domain.billing.PaymentGatewayPort;
import com.buzzanalysis.domain.billing.Subscription;
import com.buzzanalysis.domain.billing.SubscriptionPlan;
import com.buzzanalysis.domain.billing.SubscriptionRepository;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import com.buzzanalysis.infrastructure.payment.GmoPaymentProperties;
import com.buzzanalysis.infrastructure.quota.UsageQuotaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 課金プラン(改善計画No.11: 無料枠/有料枠)のアップグレード・解約・契約更新を担うアプリケーションサービス。
 */
@Service
public class SubscriptionApplicationService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionApplicationService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final UserRepository userRepository;
    private final UsageQuotaProperties usageQuotaProperties;
    private final GmoPaymentProperties gmoPaymentProperties;

    public SubscriptionApplicationService(SubscriptionRepository subscriptionRepository,
                                           PaymentGatewayPort paymentGatewayPort,
                                           UserRepository userRepository,
                                           UsageQuotaProperties usageQuotaProperties,
                                           GmoPaymentProperties gmoPaymentProperties) {
        this.subscriptionRepository = subscriptionRepository;
        this.paymentGatewayPort = paymentGatewayPort;
        this.userRepository = userRepository;
        this.usageQuotaProperties = usageQuotaProperties;
        this.gmoPaymentProperties = gmoPaymentProperties;
    }

    /**
     * FREE/PROプランの料金・利用上限。フロントエンドが独自の定数として料金を持つと表示と実際の
     * 課金額が食い違うリスクがあるため(シニアレビュー指摘)、設定値を唯一の情報源として公開する。
     */
    public BillingPlansDto getPlans() {
        return new BillingPlansDto(
                new PlanInfoDto("FREE", 0, usageQuotaProperties.getDailyOpenAiCallsFree()),
                new PlanInfoDto("PRO", gmoPaymentProperties.getProPlanMonthlyAmount(),
                        usageQuotaProperties.getDailyOpenAiCallsPro()));
    }

    @Transactional(readOnly = true)
    public SubscriptionDto getMyPlan(UUID userId) {
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseGet(() -> Subscription.createFree(userId));
        return toDto(subscription);
    }

    @Transactional
    public SubscriptionDto upgradeToPro(UUID userId, String cardToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> EntityNotFoundException.of("User", userId));

        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseGet(() -> Subscription.createFree(userId));
        if (subscription.getPlan() == SubscriptionPlan.PRO) {
            throw new BusinessRuleViolationException("既にPROプランをご利用中です");
        }

        PaymentChargeResult result =
                paymentGatewayPort.registerMemberAndStartRecurringCharge(userId, user.getEmail(), cardToken);
        if (!result.success()) {
            throw new BusinessRuleViolationException(result.errorMessage());
        }

        subscription.upgradeToPro(result.paymentProviderMemberId(), OffsetDateTime.now().plusMonths(1));
        return toDto(subscriptionRepository.save(subscription));
    }

    @Transactional
    public SubscriptionDto cancel(UUID userId) {
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .filter(s -> s.getPlan() == SubscriptionPlan.PRO)
                .orElseThrow(() -> new BusinessRuleViolationException("PROプランをご利用中ではありません"));

        paymentGatewayPort.cancelRecurringCharge(subscription.getPaymentProviderMemberId());
        subscription.cancel();
        return toDto(subscriptionRepository.save(subscription));
    }

    /**
     * 契約更新日を迎えたPRO契約に対して再課金を行う({@link com.buzzanalysis.infrastructure.scheduling.BillingRenewalScheduler}
     * から定期実行される)。課金に失敗した契約はFREEプランへ自動的にダウングレードする(督促・リトライは行わない簡易実装)。
     *
     * @return 更新に成功した契約数
     */
    @Transactional
    public int renewDueSubscriptions() {
        List<Subscription> dueSubscriptions = subscriptionRepository.findActiveProSubscriptionsDueForRenewal(OffsetDateTime.now());
        int renewedCount = 0;
        for (Subscription subscription : dueSubscriptions) {
            PaymentChargeResult result = paymentGatewayPort.chargeRenewal(subscription.getPaymentProviderMemberId());
            if (result.success()) {
                subscription.upgradeToPro(subscription.getPaymentProviderMemberId(), OffsetDateTime.now().plusMonths(1));
                renewedCount++;
            } else {
                log.warn("Subscription renewal failed for userId={}, downgrading to FREE", subscription.getUserId());
                subscription.cancel();
            }
            subscriptionRepository.save(subscription);
        }
        return renewedCount;
    }

    private SubscriptionDto toDto(Subscription subscription) {
        int dailyLimit = subscription.getPlan() == SubscriptionPlan.PRO
                ? usageQuotaProperties.getDailyOpenAiCallsPro()
                : usageQuotaProperties.getDailyOpenAiCallsFree();
        return new SubscriptionDto(subscription.getPlan(), subscription.getStatus(),
                subscription.getCurrentPeriodEnd(), dailyLimit);
    }
}
