package com.buzzanalysis.infrastructure.quota;

import com.buzzanalysis.domain.billing.Subscription;
import com.buzzanalysis.domain.billing.SubscriptionPlan;
import com.buzzanalysis.domain.billing.SubscriptionRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

/**
 * ユーザー単位・日次のOpenAI呼び出し回数をRedisでカウントする。
 * キーは {@code quota:openai:<userId>:<yyyy-MM-dd>} で、初回インクリメント時のみ25時間のTTLを設定し
 * (日付跨ぎの猶予を持たせつつ翌日以降は自動的にキーが消える)、当日分の呼び出し回数を数える。
 * 上限値はユーザーの課金プラン(改善計画No.11)によってFREE/PROで異なる。
 */
@Service
public class UsageQuotaService {

    /**
     * 利用上限超過時にBusinessRuleViolationExceptionへ付与するerrorCode。
     * フロントエンドはこのコードでアップセルCTAの表示を判定する(メッセージ文字列には依存しない)。
     */
    public static final String EXCEEDED_ERROR_CODE = "AI_USAGE_QUOTA_EXCEEDED";

    private static final String KEY_PREFIX = "quota:openai:";
    private static final Duration KEY_TTL = Duration.ofHours(25);

    private final StringRedisTemplate redisTemplate;
    private final UsageQuotaProperties properties;
    private final SubscriptionRepository subscriptionRepository;

    public UsageQuotaService(StringRedisTemplate redisTemplate, UsageQuotaProperties properties,
                              SubscriptionRepository subscriptionRepository) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * 呼び出し1回分を消費する。上限に達している場合は消費せず {@code false} を返す。
     */
    public boolean tryConsume(UUID userId) {
        String key = KEY_PREFIX + userId + ":" + LocalDate.now();
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, KEY_TTL);
        }
        return count != null && count <= resolveDailyLimit(userId);
    }

    private int resolveDailyLimit(UUID userId) {
        SubscriptionPlan plan = subscriptionRepository.findByUserId(userId)
                .map(Subscription::getPlan)
                .orElse(SubscriptionPlan.FREE);
        return plan == SubscriptionPlan.PRO ? properties.getDailyOpenAiCallsPro() : properties.getDailyOpenAiCallsFree();
    }
}
