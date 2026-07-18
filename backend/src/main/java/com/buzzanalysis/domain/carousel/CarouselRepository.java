package com.buzzanalysis.domain.carousel;

import java.util.List;
import java.util.UUID;

/** {@link Carousel} の永続化を抽象化するリポジトリ（Repositoryパターン、Phase12）。 */
public interface CarouselRepository {

    Carousel save(Carousel carousel);

    List<Carousel> findByProposalId(UUID proposalId);
}
