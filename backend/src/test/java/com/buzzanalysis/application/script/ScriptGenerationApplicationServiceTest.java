package com.buzzanalysis.application.script;

import com.buzzanalysis.application.script.dto.ScriptGenerationRequest;
import com.buzzanalysis.application.script.dto.VideoScriptDto;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.preprocessing.ContentFormat;
import com.buzzanalysis.domain.proposal.ContentProposal;
import com.buzzanalysis.domain.proposal.ContentProposalRepository;
import com.buzzanalysis.domain.script.VideoScript;
import com.buzzanalysis.domain.script.VideoScriptRepository;
import com.buzzanalysis.infrastructure.quota.UsageQuotaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
// durationSeconds不正テストは利用上限チェックの前に例外を投げるため、
// 未使用スタブの厳格チェック(UnnecessaryStubbingException)を無効化する。
@MockitoSettings(strictness = Strictness.LENIENT)
class ScriptGenerationApplicationServiceTest {

    @Mock
    private ContentProposalRepository contentProposalRepository;
    @Mock
    private AiScriptGenerationPort aiScriptGenerationPort;
    @Mock
    private VideoScriptRepository videoScriptRepository;
    @Mock
    private UsageQuotaService usageQuotaService;

    private ScriptGenerationApplicationService service;
    private UUID requestingUserId;

    @BeforeEach
    void setUp() {
        service = new ScriptGenerationApplicationService(contentProposalRepository, aiScriptGenerationPort,
                videoScriptRepository, usageQuotaService);
        requestingUserId = UUID.randomUUID();
        when(usageQuotaService.tryConsume(any())).thenReturn(true);
    }

    private ContentProposal sampleProposal(UUID id) {
        return ContentProposal.builder().id(id).generationId(UUID.randomUUID()).sequenceNumber(1)
                .title("企画").createdAt(OffsetDateTime.now()).build();
    }

    @Test
    void generate_rejectsUnsupportedDuration() {
        assertThatThrownBy(() -> service.generate(new ScriptGenerationRequest(UUID.randomUUID(), 45), requestingUserId))
                .isInstanceOf(BusinessRuleViolationException.class);
        verifyNoInteractions(contentProposalRepository, aiScriptGenerationPort, videoScriptRepository);
    }

    @Test
    void generate_throwsBusinessRuleViolation_whenDailyUsageQuotaExceeded() {
        when(usageQuotaService.tryConsume(requestingUserId)).thenReturn(false);

        assertThatThrownBy(() -> service.generate(new ScriptGenerationRequest(UUID.randomUUID(), 30), requestingUserId))
                .isInstanceOf(BusinessRuleViolationException.class);
        verifyNoInteractions(contentProposalRepository, aiScriptGenerationPort, videoScriptRepository);
    }

    @Test
    void generate_throwsNotFound_whenProposalMissing() {
        UUID proposalId = UUID.randomUUID();
        when(contentProposalRepository.findById(proposalId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generate(new ScriptGenerationRequest(proposalId, 30), requestingUserId))
                .isInstanceOf(EntityNotFoundException.class);
        verifyNoInteractions(aiScriptGenerationPort, videoScriptRepository);
    }

    @Test
    void generate_persistsScriptFromAiOutput() {
        UUID proposalId = UUID.randomUUID();
        when(contentProposalRepository.findById(proposalId)).thenReturn(Optional.of(sampleProposal(proposalId)));
        AiScriptGenerationPort.GeneratedScript generated = new AiScriptGenerationPort.GeneratedScript(
                "BGM案", "CTA案",
                List.of(new AiScriptGenerationPort.GeneratedCut(1, 0, 10, "ナレ", "テロップ", "映像指示")));
        when(aiScriptGenerationPort.generate(any(), org.mockito.ArgumentMatchers.eq(60))).thenReturn(generated);
        when(videoScriptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VideoScriptDto result = service.generate(new ScriptGenerationRequest(proposalId, 60), requestingUserId);

        assertThat(result.proposalId()).isEqualTo(proposalId);
        assertThat(result.durationSeconds()).isEqualTo(60);
        assertThat(result.bgmImage()).isEqualTo("BGM案");
        assertThat(result.cuts()).hasSize(1);
        assertThat(result.cuts().get(0).narration()).isEqualTo("ナレ");
    }

    @Test
    void findByProposalId_returnsDtosFromRepository() {
        UUID proposalId = UUID.randomUUID();
        VideoScript script = VideoScript.builder().id(UUID.randomUUID()).proposalId(proposalId)
                .durationSeconds(30).cuts(List.of()).createdAt(OffsetDateTime.now()).build();
        when(videoScriptRepository.findByProposalId(proposalId)).thenReturn(List.of(script));

        List<VideoScriptDto> result = service.findByProposalId(proposalId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).durationSeconds()).isEqualTo(30);
    }
}
