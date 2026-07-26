package com.buzzanalysis.infrastructure.security;

import com.buzzanalysis.presentation.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * ログイン/登録/パスワードリセットエンドポイントへのリクエストを、送信元IPごとに一定時間内の
 * 回数で制限する。ブルートフォース攻撃・クレデンシャルスタッフィング・パスワードリセットメールの
 * 大量送信(メール爆撃)・リセットトークンの総当たりへの対策として、認証系エンドポイントに
 * レート制限が一切無かったことへの対応。
 * IPは{@code request.getRemoteAddr()}のみを信頼する。X-Forwarded-Forは直接インターネットに
 * 露出したサーバーではクライアントが自由に詐称できるため使用しない（信頼できるリバースプロキシ配下に
 * 置く場合は、そのプロキシがヘッダーを上書きする設定を前提に別途対応する）。
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String KEY_PREFIX = "ratelimit:auth:";

    private final StringRedisTemplate redisTemplate;
    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(StringRedisTemplate redisTemplate, RateLimitProperties properties, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        boolean isRateLimited = uri.startsWith("/api/v1/auth/login")
                || uri.startsWith("/api/v1/auth/register")
                || uri.startsWith("/api/v1/auth/password-reset/");
        if (!isRateLimited) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = KEY_PREFIX + request.getRemoteAddr();
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofMinutes(properties.getAuthWindowMinutes()));
        }

        if (count != null && count > properties.getAuthMaxAttempts()) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            ErrorResponse body = ErrorResponse.of(429, "Too Many Requests",
                    "試行回数が多すぎます。" + properties.getAuthWindowMinutes() + "分後に再度お試しください。", uri);
            response.getWriter().write(objectMapper.writeValueAsString(body));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
