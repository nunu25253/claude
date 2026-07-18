package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.matching.dto.UserSearchConditionRequest;
import com.buzzanalysis.application.rankingscore.ContentRankingApplicationService;
import com.buzzanalysis.application.rankingscore.dto.RankingScoreResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ランキングAI API（AIマーケティングOS Phase7）。Phase6の一致率に加え、既存BuzzScore・いいね率・
 * 投稿鮮度・動画時間を総合した最終ランキングスコアで投稿を並び替える。
 */
@RestController
@RequestMapping("/api/v1/ranking-score")
@Tag(name = "RankingScore", description = "総合ランキングスコア算出（AIマーケティングOS Phase7）")
public class ContentRankingController {

    private final ContentRankingApplicationService contentRankingApplicationService;

    public ContentRankingController(ContentRankingApplicationService contentRankingApplicationService) {
        this.contentRankingApplicationService = contentRankingApplicationService;
    }

    @Operation(summary = "総合ランキングスコアで投稿を順位付け",
            description = "Phase6の一致率 + BuzzScore + いいね率 + 投稿鮮度 + 動画時間を重み付き平均した"
                    + "ランキングスコアで降順に並び替える。")
    @PostMapping("/rank")
    public ResponseEntity<List<RankingScoreResultDto>> rank(
            @RequestBody UserSearchConditionRequest request,
            @Parameter(description = "取得件数上限") @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(contentRankingApplicationService.rank(request.toDomain(), limit));
    }
}
