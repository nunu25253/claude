package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.script.ScriptGenerationApplicationService;
import com.buzzanalysis.application.script.dto.ScriptGenerationRequest;
import com.buzzanalysis.application.script.dto.VideoScriptDto;
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
 * 台本生成API（AIマーケティングOS Phase11）。Phase10の投稿企画を元に、AIが指定した尺(30/60/90秒)の
 * 動画台本（ナレーション/テロップ/BGMイメージ/CTA/カット構成）を生成し永続化する。
 */
@RestController
@RequestMapping("/api/v1/scripts")
@Tag(name = "Scripts", description = "台本生成AI（AIマーケティングOS Phase11）")
public class ScriptController {

    private final ScriptGenerationApplicationService scriptGenerationApplicationService;

    public ScriptController(ScriptGenerationApplicationService scriptGenerationApplicationService) {
        this.scriptGenerationApplicationService = scriptGenerationApplicationService;
    }

    @Operation(summary = "動画台本の生成",
            description = "投稿企画IDと尺(30/60/90秒のいずれか)から、AIが動画台本を生成する。")
    @PostMapping("/generate")
    public ResponseEntity<VideoScriptDto> generate(Authentication authentication,
                                                    @Valid @RequestBody ScriptGenerationRequest request) {
        return ResponseEntity.ok(scriptGenerationApplicationService.generate(request, currentUserId(authentication)));
    }

    @Operation(summary = "企画に紐づく生成済み台本の取得", description = "指定した投稿企画IDに紐づく台本一覧を取得する。")
    @GetMapping("/proposal/{proposalId}")
    public ResponseEntity<List<VideoScriptDto>> findByProposalId(@PathVariable UUID proposalId) {
        return ResponseEntity.ok(scriptGenerationApplicationService.findByProposalId(proposalId));
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
