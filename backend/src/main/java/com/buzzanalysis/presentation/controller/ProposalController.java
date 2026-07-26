package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.proposal.ProposalGenerationApplicationService;
import com.buzzanalysis.application.proposal.dto.ContentProposalDto;
import com.buzzanalysis.application.proposal.dto.ProposalGenerationRequest;
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
 * 企画生成API（AIマーケティングOS Phase10）。共通点分析結果を元にAIが投稿企画（既定20件）を生成し、
 * 生成単位（generationId）で永続化・再取得できる。
 */
@RestController
@RequestMapping("/api/v1/proposals")
@Tag(name = "Proposals", description = "企画生成AI（AIマーケティングOS Phase10）")
public class ProposalController {

    private final ProposalGenerationApplicationService proposalGenerationApplicationService;

    public ProposalController(ProposalGenerationApplicationService proposalGenerationApplicationService) {
        this.proposalGenerationApplicationService = proposalGenerationApplicationService;
    }

    @Operation(summary = "投稿企画の生成",
            description = "投稿ID配列(最大100件)から共通点分析を行い、その結果を元にAIが投稿企画をcount件(既定20件、上限20件)生成する。")
    @PostMapping("/generate")
    public ResponseEntity<List<ContentProposalDto>> generate(Authentication authentication,
                                                              @Valid @RequestBody ProposalGenerationRequest request) {
        return ResponseEntity.ok(proposalGenerationApplicationService.generate(request, currentUserId(authentication)));
    }

    @Operation(summary = "生成済み企画の取得", description = "指定したgenerationIdで生成された企画一覧を取得する。")
    @GetMapping("/{generationId}")
    public ResponseEntity<List<ContentProposalDto>> findByGenerationId(@PathVariable UUID generationId) {
        return ResponseEntity.ok(proposalGenerationApplicationService.findByGenerationId(generationId));
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
