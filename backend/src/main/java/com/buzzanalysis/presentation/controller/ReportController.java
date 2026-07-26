package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.report.ReportGenerationApplicationService;
import com.buzzanalysis.application.report.dto.ReportDto;
import com.buzzanalysis.application.report.dto.ReportHistoryItemDto;
import com.buzzanalysis.domain.report.ReportFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** AIレポート出力API（PDF/Markdown/HTML）。 */
@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "AIレポート生成 (PDF/Markdown/HTML)")
public class ReportController {

    private final ReportGenerationApplicationService reportGenerationApplicationService;

    public ReportController(ReportGenerationApplicationService reportGenerationApplicationService) {
        this.reportGenerationApplicationService = reportGenerationApplicationService;
    }

    @Operation(summary = "AIレポート生成", description = "指定した投稿の分析結果からPDF/Markdown/HTMLレポートを生成し、ダウンロードURLを返す")
    @PostMapping("/{postId}/generate")
    public ResponseEntity<ReportDto> generate(
            Authentication authentication,
            @PathVariable UUID postId,
            @Parameter(description = "出力フォーマット (pdf|markdown|html)") @RequestParam String format
    ) {
        ReportFormat reportFormat;
        try {
            reportFormat = ReportFormat.valueOf(format.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid report format: " + format + " (expected one of pdf, markdown, html)");
        }
        ReportDto result = reportGenerationApplicationService.generateReport(
                currentUserId(authentication), postId, reportFormat);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "レポート履歴一覧取得", description = "ログイン中ユーザーが生成したレポートを新しい順に返す")
    @GetMapping
    public ResponseEntity<List<ReportHistoryItemDto>> history(Authentication authentication) {
        return ResponseEntity.ok(reportGenerationApplicationService.getHistory(currentUserId(authentication)));
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
