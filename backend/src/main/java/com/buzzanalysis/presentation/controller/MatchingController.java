package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.matching.UserConditionMatchApplicationService;
import com.buzzanalysis.application.matching.dto.MatchRateResultDto;
import com.buzzanalysis.application.matching.dto.UserSearchConditionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ユーザー条件分析API（AIマーケティングOS Phase6）。キーワード・商品名・ブランド・ASP案件名・ジャンル・
 * サブジャンル・ターゲット年齢/性別・投稿形式・SNS・動画時間・投稿目的から、各投稿の一致率(0〜100)を算出する。
 */
@RestController
@RequestMapping("/api/v1/matching")
@Tag(name = "Matching", description = "ユーザー条件分析・一致率算出（AIマーケティングOS Phase6）")
public class MatchingController {

    private final UserConditionMatchApplicationService userConditionMatchApplicationService;

    public MatchingController(UserConditionMatchApplicationService userConditionMatchApplicationService) {
        this.userConditionMatchApplicationService = userConditionMatchApplicationService;
    }

    @Operation(summary = "ユーザー条件と投稿の一致率算出",
            description = "自由文条件(キーワード等)が指定されている場合は意味検索で候補を絞り込み、"
                    + "各Strategyのスコアを重み付き平均して0〜100の一致率を算出する。")
    @PostMapping("/evaluate")
    public ResponseEntity<List<MatchRateResultDto>> evaluate(
            @Valid @RequestBody UserSearchConditionRequest request,
            @Parameter(description = "取得件数上限") @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(userConditionMatchApplicationService.evaluate(request.toDomain(), limit));
    }
}
