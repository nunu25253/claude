package com.buzzanalysis.application.carousel.dto;

import com.buzzanalysis.domain.carousel.Carousel;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** {@link Carousel}（ドメイン）のapplication層向けDTO。 */
public record CarouselDto(UUID id, UUID proposalId, List<CarouselPageDto> pages, OffsetDateTime createdAt) {
    public static CarouselDto from(Carousel c) {
        return new CarouselDto(c.getId(), c.getProposalId(),
                c.getPages().stream().map(CarouselPageDto::from).toList(), c.getCreatedAt());
    }
}
