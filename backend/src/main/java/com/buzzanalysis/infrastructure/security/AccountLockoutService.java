package com.buzzanalysis.infrastructure.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;

/**
 * メールアドレス(アカウント)単位でログイン失敗回数を数え、閾値を超えると一時的にロックする。
 * {@link RateLimitFilter}の送信元IPベースの制限は、多数のIP/ボットネットに分散された
 * クレデンシャルスタッフィング(単一アカウントへ様々なパスワードを様々なIPから試す攻撃)は
 * 防げないため、これを補完する。
 *
 * <p>設計上のトレードオフ: アカウント単位のロックは、第三者が被害者のメールアドレス宛に
 * 誤ったパスワードを繰り返し送ることで、正規ユーザー自身を一時的にログイン不能にする
 * (可用性へのDoS)目的にも使われ得る。本実装は閾値・ロック時間を{@link RateLimitFilter}の
 * IP制限と同程度の緩やかな値にし、クレデンシャルスタッフィングという、より一般的で実害の大きい
 * 脅威を優先して防ぐ設計とした。より厳密な対策(段階的な遅延、通知等)が必要な場合は将来の拡張とする。
 */
@Service
public class AccountLockoutService {

    /** ロック中エラー時にBusinessRuleViolationExceptionへ付与するerrorCode。 */
    public static final String ACCOUNT_LOCKED_ERROR_CODE = "ACCOUNT_LOCKED";

    private static final String FAILURE_KEY_PREFIX = "lockout:failures:";
    private static final String LOCKED_KEY_PREFIX = "lockout:locked:";

    private final StringRedisTemplate redisTemplate;
    private final AccountLockoutProperties properties;

    public AccountLockoutService(StringRedisTemplate redisTemplate, AccountLockoutProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    /** 現在ロック中かどうかを判定する。 */
    public boolean isLocked(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(LOCKED_KEY_PREFIX + normalize(email)));
    }

    /**
     * ログイン失敗を1回記録する。失敗回数が設定された時間枠内で閾値を超えた場合、
     * 別途ロックキーを設定しロック状態にする。
     */
    public void recordFailure(String email) {
        String key = FAILURE_KEY_PREFIX + normalize(email);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofMinutes(properties.getFailureWindowMinutes()));
        }
        if (count != null && count >= properties.getMaxFailedAttempts()) {
            redisTemplate.opsForValue().set(LOCKED_KEY_PREFIX + normalize(email), "1",
                    Duration.ofMinutes(properties.getLockoutMinutes()));
        }
    }

    /** ログイン成功時に失敗カウント・ロック状態をリセットする。 */
    public void recordSuccess(String email) {
        String normalized = normalize(email);
        redisTemplate.delete(FAILURE_KEY_PREFIX + normalized);
        redisTemplate.delete(LOCKED_KEY_PREFIX + normalized);
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
