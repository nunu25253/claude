package com.buzzanalysis.domain.carousel;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AIが生成したInstagramカルーセル（Phase12）。Phase10の{@code ContentProposal}1件から生成される
 * 2〜8ページ構成（1ページ目=HOOK、最終ページ=CTA、それ以外=EXPLANATION）。
 */
public final class Carousel {

    private final UUID id;
    private final UUID proposalId;
    private final List<CarouselPage> pages;
    private final OffsetDateTime createdAt;

    private Carousel(Builder b) {
        this.id = b.id;
        this.proposalId = b.proposalId;
        this.pages = b.pages == null ? List.of() : b.pages;
        this.createdAt = b.createdAt;
    }

    public static Builder builder() {
        return new Builder();
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

    public static final class Builder {
        private UUID id;
        private UUID proposalId;
        private List<CarouselPage> pages;
        private OffsetDateTime createdAt;

        public Builder id(UUID v) {
            this.id = v;
            return this;
        }

        public Builder proposalId(UUID v) {
            this.proposalId = v;
            return this;
        }

        public Builder pages(List<CarouselPage> v) {
            this.pages = v;
            return this;
        }

        public Builder createdAt(OffsetDateTime v) {
            this.createdAt = v;
            return this;
        }

        public Carousel build() {
            return new Carousel(this);
        }
    }
}
