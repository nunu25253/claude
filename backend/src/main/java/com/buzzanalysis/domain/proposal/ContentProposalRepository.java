package com.buzzanalysis.domain.proposal;

import java.util.List;
import java.util.UUID;

/** {@link ContentProposal} の永続化を抽象化するリポジトリ（Repositoryパターン、Phase10）。 */
public interface ContentProposalRepository {

    List<ContentProposal> saveAll(List<ContentProposal> proposals);

    List<ContentProposal> findByGenerationId(UUID generationId);
}
