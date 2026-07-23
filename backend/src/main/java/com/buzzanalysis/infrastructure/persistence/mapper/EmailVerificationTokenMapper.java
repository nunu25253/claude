package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.auth.EmailVerificationToken;
import com.buzzanalysis.infrastructure.persistence.entity.EmailVerificationTokenEntity;
import org.springframework.stereotype.Component;

/** {@link EmailVerificationToken}（ドメイン）と {@link EmailVerificationTokenEntity}（JPA）の相互変換を行う。 */
@Component
public class EmailVerificationTokenMapper {

    public EmailVerificationTokenEntity toEntity(EmailVerificationToken domain) {
        return new EmailVerificationTokenEntity(domain.getId(), domain.getUserId(), domain.getTokenHash(),
                domain.getExpiresAt(), domain.getUsedAt(), domain.getCreatedAt());
    }

    public EmailVerificationToken toDomain(EmailVerificationTokenEntity entity) {
        return new EmailVerificationToken(entity.getId(), entity.getUserId(), entity.getTokenHash(),
                entity.getExpiresAt(), entity.getUsedAt(), entity.getCreatedAt());
    }
}
