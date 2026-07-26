package com.buzzanalysis.domain.script;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** {@link VideoScript} の永続化を抽象化するリポジトリ（Repositoryパターン、Phase11）。 */
public interface VideoScriptRepository {

    VideoScript save(VideoScript script);

    List<VideoScript> findByProposalId(UUID proposalId);

    Optional<VideoScript> findById(UUID id);
}
