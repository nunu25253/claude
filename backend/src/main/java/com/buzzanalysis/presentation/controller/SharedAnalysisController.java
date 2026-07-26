package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.savedanalysis.SavedAnalysisShareApplicationService;
import com.buzzanalysis.application.savedanalysis.dto.SavedAnalysisDetailDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 共有トークンによる分析結果の閲覧API。認証不要(SecurityConfigのPUBLIC_PATHSに登録済み)。
 * トークンの知得自体をアクセス権とみなす({@code /api/v1/reports/files/**}と同じ考え方)。
 */
@RestController
@RequestMapping("/api/v1/shared")
@Tag(name = "SharedAnalyses", description = "外部共有リンク経由の分析結果閲覧(認証不要)")
public class SharedAnalysisController {

    private final SavedAnalysisShareApplicationService savedAnalysisShareApplicationService;

    public SharedAnalysisController(SavedAnalysisShareApplicationService savedAnalysisShareApplicationService) {
        this.savedAnalysisShareApplicationService = savedAnalysisShareApplicationService;
    }

    @Operation(summary = "共有トークンから分析結果を取得", description = "リンクが失効済み・存在しない場合は404")
    @GetMapping("/{token}")
    public SavedAnalysisDetailDto getSharedAnalysis(@PathVariable UUID token) {
        return savedAnalysisShareApplicationService.getSharedAnalysis(token);
    }
}
