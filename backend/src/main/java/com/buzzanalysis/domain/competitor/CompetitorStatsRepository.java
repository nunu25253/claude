package com.buzzanalysis.domain.competitor;

import java.util.Optional;
import java.util.UUID;

/** CompetitorStats集約のリポジトリインターフェース。 */
public interface CompetitorStatsRepository {

    CompetitorStats save(CompetitorStats stats);

    Optional<CompetitorStats> findBySocialAccountId(UUID socialAccountId);
}
