package com.buzzanalysis.domain.evaluation;

import java.util.List;
import java.util.UUID;

/** {@link ContentEvaluation} の永続化を抽象化するリポジトリ（Repositoryパターン、Phase14）。 */
public interface ContentEvaluationRepository {

    ContentEvaluation save(ContentEvaluation evaluation);

    List<ContentEvaluation> findByProposalId(UUID proposalId);
}
