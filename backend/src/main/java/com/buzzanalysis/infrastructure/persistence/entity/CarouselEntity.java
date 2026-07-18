package com.buzzanalysis.infrastructure.persistence.entity;

import com.buzzanalysis.domain.carousel.CarouselPage;
import com.buzzanalysis.infrastructure.persistence.converter.CarouselPageListJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** {@code carousels} テーブルに対応するJPAエンティティ。 */
@Entity
@Table(name = "carousels")
public class CarouselEntity {

    @Id
    private UUID id;

    @Column(name = "proposal_id", nullable = false)
    private UUID proposalId;

    @Convert(converter = CarouselPageListJsonConverter.class)
    @Column(name = "pages", columnDefinition = "TEXT", nullable = false)
    private List<CarouselPage> pages;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected CarouselEntity() {
    }

    public CarouselEntity(UUID id, UUID proposalId, List<CarouselPage> pages, OffsetDateTime createdAt) {
        this.id = id;
        this.proposalId = proposalId;
        this.pages = pages;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProposalId() {
        return proposalId;
    }

    public List<CarouselPage> getPages() {
        return pages;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
