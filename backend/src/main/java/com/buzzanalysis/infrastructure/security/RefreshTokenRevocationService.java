package com.buzzanalysis.infrastructure.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * ログアウト時にリフレッシュトークンを失効させるためのRedisベースの denylist。
 * トークン自体はステートレス(署名検証のみ)なため、失効させたいものだけをjti(トークン固有ID)単位で
 * 記録し、TTLをトークンの残り有効期限に合わせることで、期限が来れば自動的にエントリが消える
 * ({@link com.buzzanalysis.infrastructure.quota.UsageQuotaService}と同じRedis利用パターン)。
 */
@Service
public class RefreshTokenRevocationService {

    private static final String KEY_PREFIX = "revoked:refresh-token:";

    private final StringRedisTemplate redisTemplate;

    public RefreshTokenRevocationService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** 指定jtiを、残り有効期限(秒)の間だけ失効済みとして記録する。 */
    public void revoke(String jti, long ttlSeconds) {
        if (jti == null || jti.isBlank() || ttlSeconds <= 0) {
            return;
        }
        redisTemplate.opsForValue().set(KEY_PREFIX + jti, "1", Duration.ofSeconds(ttlSeconds));
    }

    public boolean isRevoked(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + jti));
    }
}
