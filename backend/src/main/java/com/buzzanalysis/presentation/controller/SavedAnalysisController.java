package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.savedanalysis.SavedAnalysisApplicationService;
import com.buzzanalysis.application.savedanalysis.dto.SaveAnalysisCommand;
import com.buzzanalysis.application.savedanalysis.dto.SavedAnalysisDto;
import com.buzzanalysis.presentation.dto.request.SaveAnalysisRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** 保存済み分析（ブックマーク）API。ログイン中のユーザー（JWT）に紐づけて管理する。 */
@RestController
@RequestMapping("/api/v1/saved-analyses")
@Tag(name = "SavedAnalyses", description = "保存済み分析（ブックマーク）")
public class SavedAnalysisController {

    private final SavedAnalysisApplicationService savedAnalysisApplicationService;

    public SavedAnalysisController(SavedAnalysisApplicationService savedAnalysisApplicationService) {
        this.savedAnalysisApplicationService = savedAnalysisApplicationService;
    }

    @Operation(summary = "保存済み分析一覧取得")
    @GetMapping
    public ResponseEntity<List<SavedAnalysisDto>> list(Authentication authentication) {
        UUID userId = currentUserId(authentication);
        return ResponseEntity.ok(savedAnalysisApplicationService.list(userId));
    }

    @Operation(summary = "分析結果を保存")
    @PostMapping
    public ResponseEntity<SavedAnalysisDto> save(Authentication authentication, @Valid @RequestBody SaveAnalysisRequest request) {
        UUID userId = currentUserId(authentication);
        SavedAnalysisDto saved = savedAnalysisApplicationService.save(
                new SaveAnalysisCommand(userId, request.postId(), request.note()));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "保存済み分析を削除")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID id) {
        UUID userId = currentUserId(authentication);
        savedAnalysisApplicationService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
