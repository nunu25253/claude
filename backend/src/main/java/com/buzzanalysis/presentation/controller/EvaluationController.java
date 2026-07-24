package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.evaluation.PostEvaluationApplicationService;
import com.buzzanalysis.application.evaluation.dto.ContentEvaluationDto;
import com.buzzanalysis.application.evaluation.dto.PostEvaluationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 投稿評価API（AIマーケティングOS Phase14）。ユーザーが作成した（またはAIが生成した）台本・
 * カルーセル等の投稿内容を評価する。企画ID(proposalId)を指定した場合のみ元企画との一致率を算出する。
 */
@RestController
@RequestMapping("/api/v1/evaluations")
@Tag(name = "Evaluations", description = "投稿評価AI（AIマーケティングOS Phase14）")
public class EvaluationController {

    private final PostEvaluationApplicationService postEvaluationApplicationService;

    public EvaluationController(PostEvaluationApplicationService postEvaluationApplicationService) {
        this.postEvaluationApplicationService = postEvaluationApplicationService;
    }

    @Operation(summary = "投稿内容の評価",
            description = "タイトル/フック/構成/CTA/ターゲットを評価する。proposalId指定時のみ元企画との一致率を算出する。")
    @PostMapping
    public ResponseEntity<ContentEvaluationDto> evaluate(Authentication authentication,
                                                          @Valid @RequestBody PostEvaluationRequest request) {
        return ResponseEntity.ok(postEvaluationApplicationService.evaluate(request, currentUserId(authentication)));
    }

    @Operation(summary = "企画に紐づく評価履歴の取得", description = "指定した投稿企画IDに紐づく評価履歴一覧を取得する。")
    @GetMapping("/proposal/{proposalId}")
    public ResponseEntity<List<ContentEvaluationDto>> findByProposalId(@PathVariable UUID proposalId) {
        return ResponseEntity.ok(postEvaluationApplicationService.findByProposalId(proposalId));
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
