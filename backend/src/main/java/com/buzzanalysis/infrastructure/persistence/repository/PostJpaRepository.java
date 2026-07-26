package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.PlatformEnum;
import com.buzzanalysis.infrastructure.persistence.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPAによる {@link PostEntity} の永続化アクセス。
 * キーワード/ハッシュタグ検索は {@link JpaSpecificationExecutor} を使って動的に組み立てる。
 */
public interface PostJpaRepository extends JpaRepository<PostEntity, UUID>, JpaSpecificationExecutor<PostEntity> {

    Optional<PostEntity> findByPlatformAndExternalId(PlatformEnum platform, String externalId);

    Optional<PostEntity> findByUrl(String url);
}
