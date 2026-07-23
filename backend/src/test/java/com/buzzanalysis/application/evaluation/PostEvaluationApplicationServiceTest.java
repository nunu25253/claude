package com.buzzanalysis.application.evaluation;

import com.buzzanalysis.application.evaluation.dto.ContentEvaluationDto;
import com.buzzanalysis.application.evaluation.dto.PostEvaluationRequest;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.evaluation.ContentEvaluationRepository;
import com.buzzanalysis.domain.proposal.ContentProposal;
import com.buzzanalysis.domain.proposal.ContentProposalRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostEvaluationApplicationServiceTest {

    @Mock
    private ContentProposalRepository contentProposalRepository;
    @Mock
    private AiPostEvaluationPort aiPostEvaluationPort;
    @Mock
    private ContentEvaluationRepository contentEvaluationRepository;
    @Mock
    private UsageQuotaService usageQuotaService;

    private PostEvaluationApplicationService service;
    private UUID requestingUserId;

    @BeforeEach
    void setUp() {
        service = new PostEvaluationApplicationService(contentProposalRepository, aiPostEvaluationPort,
                contentEvaluationRepository, usageQuotaService);
        requestingUserId = UUID.randomUUID();
        when(usageQuotaService.tryConsume(any())).thenReturn(true);
    }

    @Test
    void evaluate_withoutProposalId_passesEmptyReferenceToAiPort() {
        when(aiPostEvaluationPort.evaluate(any(), eq(Optional.empty()))).thenReturn(
                new AiPostEvaluationPort.AiEvaluationOutput(null, "ターゲット", List.of("提案"), "フック改善", "CTA改善", 55));
        when(contentEvaluationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ContentEvaluationDto result = service.evaluate(
                new PostEvaluationRequest(null, "タイトル", "フック", "構成", "CTA", "ターゲット"), requestingUserId);

        assertThat(result.proposalId()).isNull();
        assertThat(result.matchRatePercent()).isNull();
        assertThat(result.predictedScore()).isEqualTo(55);
        verifyNoInteractions(contentProposalRepository);
    }

    @Test
    void evaluate_throwsBusinessRuleViolation_whenDailyUsageQuotaExceeded() {
        when(usageQuotaService.tryConsume(requestingUserId)).thenReturn(false);

        assertThatThrownBy(() -> service.evaluate(
                new PostEvaluationRequest(null, "タイトル", "フック", "構成", "CTA", "ターゲット"), requestingUserId))
                .isInstanceOf(BusinessRuleViolationException.class);
        verifyNoInteractions(contentProposalRepository, aiPostEvaluationPort, contentEvaluationRepository);
    }

    @Test
    void evaluate_withProposalId_fetchesProposalAndPassesToAiPort() {
        UUID proposalId = UUID.randomUUID();
        ContentProposal proposal = ContentProposal.builder().id(proposalId).generationId(UUID.randomUUID())
                .sequenceNumber(1).title("企画").createdAt(OffsetDateTime.now()).build();
        when(contentProposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(aiPostEvaluationPort.evaluate(any(), any())).thenReturn(
                new AiPostEvaluationPort.AiEvaluationOutput(80.0, "ターゲット", List.of(), "フック改善", "CTA改善", 60));
        when(contentEvaluationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ContentEvaluationDto result = service.evaluate(
                new PostEvaluationRequest(proposalId, "タイトル", "フック", "構成", "CTA", "ターゲット"), requestingUserId);

        assertThat(result.proposalId()).isEqualTo(proposalId);
        assertThat(result.matchRatePercent()).isEqualTo(80.0);
        verify(aiPostEvaluationPort).evaluate(any(), eq(Optional.of(
                com.buzzanalysis.application.proposal.dto.ContentProposalDto.from(proposal))));
    }

    @Test
    void evaluate_throwsNotFound_whenProposalIdInvalid() {
        UUID proposalId = UUID.randomUUID();
        when(contentProposalRepository.findById(proposalId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.evaluate(
                new PostEvaluationRequest(proposalId, "タイトル", "フック", "構成", "CTA", "ターゲット"), requestingUserId))
                .isInstanceOf(EntityNotFoundException.class);
        verifyNoInteractions(aiPostEvaluationPort, contentEvaluationRepository);
    }

    @Test
    void findByProposalId_returnsDtosFromRepository() {
        UUID proposalId = UUID.randomUUID();
        when(contentEvaluationRepository.findByProposalId(proposalId)).thenReturn(List.of());

        List<ContentEvaluationDto> result = service.findByProposalId(proposalId);

        assertThat(result).isEmpty();
    }
}
