package com.buzzanalysis.infrastructure.security;

import com.buzzanalysis.application.auth.TokenProvider;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * {@link TokenProvider} のjjwtによるJWT実装。アクセストークン/リフレッシュトークンは
 * {@code type} クレームで区別し、リフレッシュ検証時に種別を確認することでトークンの使い回しを防ぐ。
 * 各トークンには一意な{@code jti}クレームを持たせ、{@link RefreshTokenRevocationService}による
 * ログアウト時の失効(denylist)を可能にしている。
 */
@Component
public class JwtTokenProvider implements TokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE = "role";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final JwtProperties properties;
    private final SecretKey signingKey;
    private final RefreshTokenRevocationService revocationService;

    public JwtTokenProvider(JwtProperties properties, RefreshTokenRevocationService revocationService) {
        this.properties = properties;
        this.revocationService = revocationService;
        this.signingKey = Keys.hmacShaKeyFor(pad(properties.getSecret()).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public IssuedToken generateAccessToken(User user) {
        long expiresInSeconds = properties.getAccessTokenExpirationMinutes() * 60;
        String token = buildToken(user, TYPE_ACCESS, expiresInSeconds);
        return new IssuedToken(token, expiresInSeconds);
    }

    @Override
    public IssuedToken generateRefreshToken(User user) {
        long expiresInSeconds = properties.getRefreshTokenExpirationDays() * 24 * 60 * 60;
        String token = buildToken(user, TYPE_REFRESH, expiresInSeconds);
        return new IssuedToken(token, expiresInSeconds);
    }

    @Override
    public UUID validateRefreshTokenAndGetUserId(String refreshToken) {
        Claims claims = parseClaims(refreshToken);
        if (!TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new BusinessRuleViolationException("Provided token is not a refresh token");
        }
        if (revocationService.isRevoked(claims.getId())) {
            throw new BusinessRuleViolationException("Refresh token has been revoked");
        }
        return UUID.fromString(claims.getSubject());
    }

    @Override
    public void revokeRefreshToken(String refreshToken) {
        try {
            Claims claims = parseClaims(refreshToken);
            if (!TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class))) {
                return;
            }
            long ttlSeconds = (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;
            revocationService.revoke(claims.getId(), ttlSeconds);
        } catch (Exception e) {
            // 既に無効/期限切れのトークンは失効させる必要が無い(べき等なログアウト)。
            log.debug("Ignoring revocation of an already invalid refresh token: {}", e.getMessage());
        }
    }

    /** アクセストークンを検証してクレームを返す（{@link com.buzzanalysis.infrastructure.security.JwtAuthenticationFilter} から利用）。 */
    public Claims validateAccessTokenAndGetClaims(String accessToken) {
        Claims claims = parseClaims(accessToken);
        if (!TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new BusinessRuleViolationException("Provided token is not an access token");
        }
        return claims;
    }

    private String buildToken(User user, String type, long expiresInSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer(properties.getIssuer())
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_TYPE, type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expiresInSeconds)))
                .signWith(signingKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessRuleViolationException("Invalid or expired token");
        }
    }

    /** HMAC-SHA256に必要な最小鍵長(32byte)を満たすようパディングする。 */
    private String pad(String secret) {
        StringBuilder sb = new StringBuilder(secret);
        while (sb.length() < 32) {
            sb.append(secret);
        }
        return sb.toString();
    }
}
