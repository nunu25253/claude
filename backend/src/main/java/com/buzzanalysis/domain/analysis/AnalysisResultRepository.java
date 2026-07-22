package com.buzzanalysis.domain.analysis;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** AnalysisResult集約のリポジトリインターフェース。 */
public interface AnalysisResultRepository {

    AnalysisResult save(AnalysisResult result);

    Optional<AnalysisResult> findByPostId(UUID postId);

    /** 複数の投稿IDに対応するAI分析結果をまとめて取得する（一覧画面でのN+1回避用）。 */
    List<AnalysisResult> findByPostIdIn(List<UUID> postIds);

    Optional<AnalysisResult> findById(UUID id);
}
