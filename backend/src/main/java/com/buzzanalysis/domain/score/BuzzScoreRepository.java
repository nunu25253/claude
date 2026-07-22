package com.buzzanalysis.domain.score;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** BuzzScore集約のリポジトリインターフェース。 */
public interface BuzzScoreRepository {

    BuzzScore save(BuzzScore buzzScore);

    Optional<BuzzScore> findByPostId(UUID postId);

    /** 複数の投稿IDに対応するBuzzScoreをまとめて取得する（一覧画面でのN+1回避用）。 */
    List<BuzzScore> findByPostIdIn(List<UUID> postIds);
}
