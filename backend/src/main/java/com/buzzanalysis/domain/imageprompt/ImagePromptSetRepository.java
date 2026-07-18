package com.buzzanalysis.domain.imageprompt;

import java.util.List;
import java.util.UUID;

/** {@link ImagePromptSet} の永続化を抽象化するリポジトリ（Repositoryパターン、Phase13）。 */
public interface ImagePromptSetRepository {

    ImagePromptSet save(ImagePromptSet promptSet);

    List<ImagePromptSet> findBySource(ImagePromptSourceType sourceType, UUID sourceId);
}
