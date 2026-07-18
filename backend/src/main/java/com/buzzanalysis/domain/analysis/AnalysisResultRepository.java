package com.buzzanalysis.domain.analysis;

import java.util.Optional;
import java.util.UUID;

/** AnalysisResult集約のリポジトリインターフェース。 */
public interface AnalysisResultRepository {

    AnalysisResult save(AnalysisResult result);

    Optional<AnalysisResult> findByPostId(UUID postId);

    Optional<AnalysisResult> findById(UUID id);
}
