package com.buzzanalysis.domain.savedanalysis;

import java.util.Optional;
import java.util.UUID;

/** SavedAnalysisShareLink集約のリポジトリインターフェース。 */
public interface SavedAnalysisShareLinkRepository {

    SavedAnalysisShareLink save(SavedAnalysisShareLink link);

    Optional<SavedAnalysisShareLink> findById(UUID id);

    /** 指定された保存済み分析に対する有効(未失効)なリンクを1件返す(無ければempty)。 */
    Optional<SavedAnalysisShareLink> findActiveBySavedAnalysisId(UUID savedAnalysisId);
}
