package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.normalization.NormalizationApplicationService;
import com.buzzanalysis.application.normalization.dto.NormalizedPostDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 投稿データ正規化API（AIマーケティングOS Phase1）。
 * Instagram/TikTok/Xなどプラットフォームごとに異なるデータ構造を統一した表現を返す。
 */
@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Normalization", description = "投稿データの正規化（プラットフォーム非依存化）")
public class NormalizationController {

    private final NormalizationApplicationService normalizationApplicationService;

    public NormalizationController(NormalizationApplicationService normalizationApplicationService) {
        this.normalizationApplicationService = normalizationApplicationService;
    }

    @Operation(summary = "投稿の正規化データ取得",
            description = "指定した投稿をプラットフォーム非依存の正規化済みデータ(NormalizedPost)として返す。"
                    + "未計測の指標(viewCount/shareCount)はnullとなり、unmeasuredMetricsに列挙される。")
    @GetMapping("/{postId}/normalized")
    public ResponseEntity<NormalizedPostDto> getNormalized(@PathVariable UUID postId) {
        return ResponseEntity.ok(normalizationApplicationService.normalize(postId));
    }
}
