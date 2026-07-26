package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.notification.Notification;
import com.buzzanalysis.infrastructure.persistence.entity.NotificationEntity;
import org.springframework.stereotype.Component;

/** {@link Notification}(ドメイン)と {@link NotificationEntity}(JPA)の相互変換を行う。 */
@Component
public class NotificationMapper {

    public NotificationEntity toEntity(Notification domain) {
        return new NotificationEntity(domain.getId(), domain.getUserId(), domain.getType(), domain.getTitle(),
                domain.getBody(), domain.getLink(), domain.getCreatedAt(), domain.getReadAt());
    }

    public Notification toDomain(NotificationEntity entity) {
        return new Notification(entity.getId(), entity.getUserId(), entity.getType(), entity.getTitle(),
                entity.getBody(), entity.getLink(), entity.getCreatedAt(), entity.getReadAt());
    }
}
