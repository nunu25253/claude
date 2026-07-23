package com.buzzanalysis.domain.savedanalysis;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** SavedAnalysis集約のリポジトリインターフェース。 */
public interface SavedAnalysisRepository {

    SavedAnalysis save(SavedAnalysis savedAnalysis);

    List<SavedAnalysis> findByUserId(UUID userId);

    Optional<SavedAnalysis> findById(UUID id);

    void deleteById(UUID id);

    /** しきい値アラートが設定済みかつ未通知のものを一括取得する(バッチ通知処理用)。 */
    List<SavedAnalysis> findPendingAlerts();
}
