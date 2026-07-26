package com.buzzanalysis.domain.score;

import java.util.List;
import java.util.UUID;

/** BuzzScore履歴のリポジトリインターフェース。追記のみ(更新・削除は行わない)。 */
public interface BuzzScoreHistoryRepository {

    BuzzScoreHistoryEntry save(BuzzScoreHistoryEntry entry);

    /** 指定投稿の履歴を計算日時の昇順(古い順)で返す。 */
    List<BuzzScoreHistoryEntry> findByPostIdOrderByCalculatedAtAsc(UUID postId);

    /**
     * 2回以上再分析された投稿のIDを、直近に再分析されたもの順に最大{@code limit}件返す
     * (AI提案の的中率算出用: 初回分析→AIの改善提案→再分析、という利用パターンを経た投稿群)。
     */
    List<UUID> findPostIdsWithAtLeastTwoEntries(int limit);
}
