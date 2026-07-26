package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.ranking.RankingApplicationService;
import com.buzzanalysis.application.ranking.dto.RankingEntryDto;
import com.buzzanalysis.application.ranking.dto.RankingQuery;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.ranking.RankingType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** ランキングAPI（急上昇/週間/月間、ジャンル別、SNS別）。 */
@RestController
@RequestMapping("/api/v1/rankings")
@Tag(name = "Rankings", description = "急上昇/週間/月間/ジャンル別/SNS別ランキング")
public class RankingController {

    private final RankingApplicationService rankingApplicationService;

    public RankingController(RankingApplicationService rankingApplicationService) {
        this.rankingApplicationService = rankingApplicationService;
    }

    @Operation(summary = "ランキング取得")
    @GetMapping
    public ResponseEntity<List<RankingEntryDto>> getRankings(
            @Parameter(description = "ランキング種別 (trending|weekly|monthly)") @RequestParam String type,
            @Parameter(description = "ジャンル") @RequestParam(required = false) String genre,
            @Parameter(description = "プラットフォーム") @RequestParam(required = false) Platform platform,
            @Parameter(description = "取得件数上限") @RequestParam(defaultValue = "20") int limit
    ) {
        RankingType rankingType;
        try {
            rankingType = RankingType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ranking type: " + type
                    + " (expected one of trending, weekly, monthly)");
        }
        RankingQuery query = new RankingQuery(rankingType, genre, platform, limit);
        return ResponseEntity.ok(rankingApplicationService.getRankings(query));
    }
}
