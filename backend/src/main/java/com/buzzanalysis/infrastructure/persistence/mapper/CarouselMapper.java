package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.carousel.Carousel;
import com.buzzanalysis.infrastructure.persistence.entity.CarouselEntity;
import org.springframework.stereotype.Component;

/** {@link Carousel}（ドメイン）と {@link CarouselEntity}（JPA）の相互変換を行う。 */
@Component
public class CarouselMapper {

    public CarouselEntity toEntity(Carousel c) {
        return new CarouselEntity(c.getId(), c.getProposalId(), c.getPages(), c.getCreatedAt());
    }

    public Carousel toDomain(CarouselEntity entity) {
        return Carousel.builder()
                .id(entity.getId())
                .proposalId(entity.getProposalId())
                .pages(entity.getPages())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
