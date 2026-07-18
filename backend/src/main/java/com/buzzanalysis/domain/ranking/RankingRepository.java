package com.buzzanalysis.domain.ranking;

import com.buzzanalysis.domain.platform.Platform;

import java.util.List;

/** Ranking集約のリポジトリインターフェース。 */
public interface RankingRepository {

    List<Ranking> save(List<Ranking> rankings);

    /** 指定した種別のランキングを全件削除する（再計算前のクリアに使用）。 */
    void deleteByType(RankingType type);

    /**
     * 種別・ジャンル・プラットフォームで絞り込んだランキングを順位昇順で取得する。
     * genre / platform はnull許容（フィルタなし）。
     */
    List<Ranking> findByFilters(RankingType type, String genre, Platform platform, int limit);
}
