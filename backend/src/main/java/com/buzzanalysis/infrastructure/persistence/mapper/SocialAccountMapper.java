package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.account.SocialAccount;
import com.buzzanalysis.infrastructure.persistence.entity.SocialAccountEntity;
import org.springframework.stereotype.Component;

/** {@link SocialAccount}（ドメイン）と {@link SocialAccountEntity}（JPA）の相互変換を行う。 */
@Component
public class SocialAccountMapper {

    private final PlatformMapper platformMapper;

    public SocialAccountMapper(PlatformMapper platformMapper) {
        this.platformMapper = platformMapper;
    }

    public SocialAccountEntity toEntity(SocialAccount account) {
        return new SocialAccountEntity(
                account.getId(), platformMapper.toEntity(account.getPlatform()), account.getExternalAccountId(),
                account.getUsername(), account.getDisplayName(), account.getProfileUrl(),
                account.getFollowerCount(), account.getPostCount(), account.getCreatedAt(), account.getUpdatedAt()
        );
    }

    public SocialAccount toDomain(SocialAccountEntity entity) {
        return new SocialAccount(
                entity.getId(), platformMapper.toDomain(entity.getPlatform()), entity.getExternalAccountId(),
                entity.getUsername(), entity.getDisplayName(), entity.getProfileUrl(),
                entity.getFollowerCount(), entity.getPostCount(), entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }
}
