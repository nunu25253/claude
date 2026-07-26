package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.organization.OrganizationApplicationService;
import com.buzzanalysis.application.organization.dto.OrganizationDto;
import com.buzzanalysis.application.organization.dto.OrganizationMemberDto;
import com.buzzanalysis.application.organization.dto.TeamSavedAnalysisDto;
import com.buzzanalysis.presentation.dto.request.CreateOrganizationRequest;
import com.buzzanalysis.presentation.dto.request.InviteMemberRequest;
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

/** 組織(チーム)機能API。マルチアカウント/チーム機能の土台(改善計画No.24)。 */
@RestController
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organizations", description = "組織(チーム)の作成・メンバー管理・保存済み分析の共有閲覧")
public class OrganizationController {

    private final OrganizationApplicationService organizationApplicationService;

    public OrganizationController(OrganizationApplicationService organizationApplicationService) {
        this.organizationApplicationService = organizationApplicationService;
    }

    @Operation(summary = "組織の作成", description = "作成したユーザーがOWNERとして登録される")
    @PostMapping
    public ResponseEntity<OrganizationDto> create(Authentication authentication,
                                                   @Valid @RequestBody CreateOrganizationRequest request) {
        OrganizationDto created = organizationApplicationService.create(currentUserId(authentication), request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "自分が所属する組織の一覧取得")
    @GetMapping("/mine")
    public ResponseEntity<List<OrganizationDto>> listMine(Authentication authentication) {
        return ResponseEntity.ok(organizationApplicationService.listMine(currentUserId(authentication)));
    }

    @Operation(summary = "組織のメンバー一覧取得", description = "組織に所属していれば誰でも閲覧可能")
    @GetMapping("/{organizationId}/members")
    public ResponseEntity<List<OrganizationMemberDto>> listMembers(Authentication authentication,
                                                                    @PathVariable UUID organizationId) {
        return ResponseEntity.ok(
                organizationApplicationService.listMembers(organizationId, currentUserId(authentication)));
    }

    @Operation(summary = "メンバーの招待", description = "既存の登録ユーザーをメールアドレスで指定して招待する。OWNERのみ実行可能")
    @PostMapping("/{organizationId}/members")
    public ResponseEntity<OrganizationMemberDto> inviteMember(Authentication authentication,
                                                               @PathVariable UUID organizationId,
                                                               @Valid @RequestBody InviteMemberRequest request) {
        OrganizationMemberDto member = organizationApplicationService.inviteMember(
                organizationId, currentUserId(authentication), request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    @Operation(summary = "メンバーの削除", description = "OWNERのみ実行可能。組織に残る唯一のOWNERは削除できない")
    @DeleteMapping("/{organizationId}/members/{userId}")
    public ResponseEntity<Void> removeMember(Authentication authentication,
                                              @PathVariable UUID organizationId,
                                              @PathVariable UUID userId) {
        organizationApplicationService.removeMember(organizationId, currentUserId(authentication), userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "チーム内の保存済み分析一覧取得",
            description = "組織メンバー全員の保存済み分析(ブックマーク)を、誰が保存したかと合わせて返す")
    @GetMapping("/{organizationId}/saved-analyses")
    public ResponseEntity<List<TeamSavedAnalysisDto>> listTeamSavedAnalyses(Authentication authentication,
                                                                             @PathVariable UUID organizationId) {
        return ResponseEntity.ok(
                organizationApplicationService.listTeamSavedAnalyses(organizationId, currentUserId(authentication)));
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
