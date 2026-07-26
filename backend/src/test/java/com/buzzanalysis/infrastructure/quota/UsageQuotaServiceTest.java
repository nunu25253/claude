package com.buzzanalysis.infrastructure.quota;

import com.buzzanalysis.domain.billing.Subscription;
import com.buzzanalysis.domain.billing.SubscriptionPlan;
import com.buzzanalysis.domain.billing.SubscriptionRepository;
import com.buzzanalysis.domain.billing.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/** {@link UsageQuotaService} の単体テスト。課金プラン(改善計画No.11)によって上限値が変わることを検証する。 */
@ExtendWith(MockitoExtension.class)
class UsageQuotaServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private SubscriptionRepository subscriptionRepository;

    private UsageQuotaService service;
    private UUID userId;

    @BeforeEach
    void setUp() {
        UsageQuotaProperties properties = new UsageQuotaProperties();
        properties.setDailyOpenAiCallsFree(2);
        properties.setDailyOpenAiCallsPro(5);
        service = new UsageQuotaService(redisTemplate, properties, subscriptionRepository);
        userId = UUID.randomUUID();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void tryConsume_allowsUpToFreeLimit_whenUserHasNoSubscription() {
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(valueOperations.increment(anyString())).thenReturn(1L, 2L, 3L);

        assertThat(service.tryConsume(userId)).isTrue();
        assertThat(service.tryConsume(userId)).isTrue();
        assertThat(service.tryConsume(userId)).isFalse();
    }

    @Test
    void tryConsume_allowsHigherLimit_whenUserIsPro() {
        Subscription proSubscription = new Subscription(UUID.randomUUID(), userId, SubscriptionPlan.PRO,
                SubscriptionStatus.ACTIVE, "gmo-member-1", OffsetDateTime.now().plusDays(20),
                OffsetDateTime.now(), OffsetDateTime.now());
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.of(proSubscription));
        when(valueOperations.increment(anyString())).thenReturn(3L);

        assertThat(service.tryConsume(userId)).isTrue();
    }
}
