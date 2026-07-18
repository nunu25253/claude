package com.buzzanalysis.domain.score;

import java.util.Optional;
import java.util.UUID;

/** BuzzScore集約のリポジトリインターフェース。 */
public interface BuzzScoreRepository {

    BuzzScore save(BuzzScore buzzScore);

    Optional<BuzzScore> findByPostId(UUID postId);
}
