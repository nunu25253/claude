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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class ScriptGenerationApplicationServiceTest {

    @Mock
    private ContentProposalRepository contentProposalRepository;
    @Mock
    private AiScriptGenerationPort aiScriptGenerationPort;
    @Mock
    private VideoScriptRepository videoScriptRepository;

    private ScriptGenerationApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ScriptGenerationApplicationService(contentProposalRepository, aiScriptGenerationPort,
                videoScriptRepository);
    }

    private ContentProposal sampleProposal(UUID id) {
        return ContentProposal.builder().id(id).generationId(UUID.randomUUID()).sequenceNumber(1)
                .title("企画").createdAt(OffsetDateTime.now()).build();
    }

    @Test
    void generate_rejectsUnsupportedDuration() {
        assertThatThrownBy(() -> service.generate(new ScriptGenerationRequest(UUID.randomUUID(), 45)))
                .isInstanceOf(BusinessRuleViolationException.class);
        verifyNoInteractions(contentProposalRepository, aiScriptGenerationPort, videoScriptRepository);
    }

    @Test
    void generate_throwsNotFound_whenProposalMissing() {
        UUID proposalId = UUID.randomUUID();
        when(contentProposalRepository.findById(proposalId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generate(new ScriptGenerationRequest(proposalId, 30)))
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

        VideoScriptDto result = service.generate(new ScriptGenerationRequest(proposalId, 60));

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
