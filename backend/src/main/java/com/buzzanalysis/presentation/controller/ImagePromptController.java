package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.imageprompt.ImagePromptGenerationApplicationService;
import com.buzzanalysis.application.imageprompt.dto.ImagePromptSetDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 画像生成プロンプトAPI（AIマーケティングOS Phase13）。Phase11の動画台本・Phase12のカルーセルの
 * 各visualDirectionを元に、画像生成AI向けの具体的なプロンプト文字列をAIが生成する
 * （実際の画像生成は行わない。コスト・著作権上の判断により、プロンプト文字列の提供に留める）。
 */
@RestController
@RequestMapping("/api/v1/image-prompts")
@Tag(name = "ImagePrompts", description = "画像生成プロンプト（AIマーケティングOS Phase13）")
public class ImagePromptController {

    private final ImagePromptGenerationApplicationService imagePromptGenerationApplicationService;

    public ImagePromptController(ImagePromptGenerationApplicationService imagePromptGenerationApplicationService) {
        this.imagePromptGenerationApplicationService = imagePromptGenerationApplicationService;
    }

    @Operation(summary = "動画台本からの画像生成プロンプト作成",
            description = "指定した台本の各カットのvisualDirectionを元に、画像生成プロンプトを生成する。")
    @PostMapping("/scripts/{scriptId}")
    public ResponseEntity<ImagePromptSetDto> generateForScript(@PathVariable UUID scriptId) {
        return ResponseEntity.ok(imagePromptGenerationApplicationService.generateForScript(scriptId));
    }

    @Operation(summary = "カルーセルからの画像生成プロンプト作成",
            description = "指定したカルーセルの各ページのvisualDirectionを元に、画像生成プロンプトを生成する。")
    @PostMapping("/carousels/{carouselId}")
    public ResponseEntity<ImagePromptSetDto> generateForCarousel(@PathVariable UUID carouselId) {
        return ResponseEntity.ok(imagePromptGenerationApplicationService.generateForCarousel(carouselId));
    }
}
