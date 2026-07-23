package com.buzzanalysis.domain.score;

import java.util.List;
import java.util.UUID;

/** BuzzScore履歴のリポジトリインターフェース。追記のみ(更新・削除は行わない)。 */
public interface BuzzScoreHistoryRepository {

    BuzzScoreHistoryEntry save(BuzzScoreHistoryEntry entry);

    /** 指定投稿の履歴を計算日時の昇順(古い順)で返す。 */
    List<BuzzScoreHistoryEntry> findByPostIdOrderByCalculatedAtAsc(UUID postId);
}
