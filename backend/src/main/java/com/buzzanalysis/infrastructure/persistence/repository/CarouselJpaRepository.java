package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.CarouselEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Spring Data JPAによる {@link CarouselEntity} の永続化アクセス。 */
public interface CarouselJpaRepository extends JpaRepository<CarouselEntity, UUID> {

    List<CarouselEntity> findByProposalId(UUID proposalId);
}
