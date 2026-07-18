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
}
