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

    /** 保存済み分析を1件以上持つユーザーIDの一覧を取得する(週次ダイジェスト配信対象の決定用)。 */
    List<UUID> findDistinctUserIds();
}
