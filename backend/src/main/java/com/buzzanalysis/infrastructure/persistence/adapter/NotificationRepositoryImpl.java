package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.notification.Notification;
import com.buzzanalysis.domain.notification.NotificationRepository;
import com.buzzanalysis.infrastructure.persistence.entity.NotificationEntity;
import com.buzzanalysis.infrastructure.persistence.mapper.NotificationMapper;
import com.buzzanalysis.infrastructure.persistence.repository.NotificationJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** {@link NotificationRepository} のJPA実装(Repositoryパターン)。 */
@Repository
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;
    private final NotificationMapper mapper;

    public NotificationRepositoryImpl(NotificationJpaRepository jpaRepository, NotificationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Notification save(Notification notification) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(notification)));
    }

    @Override
    public List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, int limit) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit)).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public long countUnreadByUserId(UUID userId) {
        return jpaRepository.countByUserIdAndReadAtIsNull(userId);
    }

    @Override
    public void markAllReadByUserId(UUID userId) {
        OffsetDateTime now = OffsetDateTime.now();
        List<NotificationEntity> unread = jpaRepository.findByUserIdAndReadAtIsNull(userId);
        List<NotificationEntity> updated = unread.stream()
                .map(entity -> new NotificationEntity(entity.getId(), entity.getUserId(), entity.getType(),
                        entity.getTitle(), entity.getBody(), entity.getLink(), entity.getCreatedAt(), now))
                .toList();
        jpaRepository.saveAll(updated);
    }
}
