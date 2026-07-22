package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.auth.PasswordResetToken;
import com.buzzanalysis.infrastructure.persistence.entity.PasswordResetTokenEntity;
import org.springframework.stereotype.Component;

/** {@link PasswordResetToken}（ドメイン）と {@link PasswordResetTokenEntity}（JPA）の相互変換を行う。 */
@Component
public class PasswordResetTokenMapper {

    public PasswordResetTokenEntity toEntity(PasswordResetToken domain) {
        return new PasswordResetTokenEntity(domain.getId(), domain.getUserId(), domain.getTokenHash(),
                domain.getExpiresAt(), domain.getUsedAt(), domain.getCreatedAt());
    }

    public PasswordResetToken toDomain(PasswordResetTokenEntity entity) {
        return new PasswordResetToken(entity.getId(), entity.getUserId(), entity.getTokenHash(),
                entity.getExpiresAt(), entity.getUsedAt(), entity.getCreatedAt());
    }
}
