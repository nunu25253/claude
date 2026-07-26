package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.user.Role;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

/** {@link User}（ドメイン）と {@link UserEntity}（JPA）の相互変換を行う。 */
@Component
public class UserMapper {

    public UserEntity toEntity(User user) {
        return new UserEntity(
                user.getId(), user.getEmail(), user.getPasswordHash(), user.getDisplayName(),
                toEntityRole(user.getRole()), user.isEmailVerified(), user.getCreatedAt(), user.getUpdatedAt(),
                user.getTrialAnalysisUsedAt()
        );
    }

    public User toDomain(UserEntity entity) {
        return new User(
                entity.getId(), entity.getEmail(), entity.getPasswordHash(), entity.getDisplayName(),
                toDomainRole(entity.getRole()), entity.isEmailVerified(), entity.getCreatedAt(), entity.getUpdatedAt(),
                entity.getTrialAnalysisUsedAt()
        );
    }

    private UserEntity.RoleEnum toEntityRole(Role role) {
        return role == Role.ADMIN ? UserEntity.RoleEnum.ADMIN : UserEntity.RoleEnum.USER;
    }

    private Role toDomainRole(UserEntity.RoleEnum role) {
        return role == UserEntity.RoleEnum.ADMIN ? Role.ADMIN : Role.USER;
    }
}
