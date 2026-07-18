package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.carousel.Carousel;
import com.buzzanalysis.domain.carousel.CarouselRepository;
import com.buzzanalysis.infrastructure.persistence.mapper.CarouselMapper;
import com.buzzanalysis.infrastructure.persistence.repository.CarouselJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/** {@link CarouselRepository} のJPA実装（Repositoryパターン）。 */
@Repository
public class CarouselRepositoryImpl implements CarouselRepository {

    private final CarouselJpaRepository jpaRepository;
    private final CarouselMapper mapper;

    public CarouselRepositoryImpl(CarouselJpaRepository jpaRepository, CarouselMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Carousel save(Carousel carousel) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(carousel)));
    }

    @Override
    public List<Carousel> findByProposalId(UUID proposalId) {
        return jpaRepository.findByProposalId(proposalId).stream().map(mapper::toDomain).toList();
    }
}
