package com.buzzanalysis.application.proposal;

import com.buzzanalysis.application.commonality.CommonalityAnalysisApplicationService;
import com.buzzanalysis.application.commonality.dto.CommonalityAnalysisResultDto;
import com.buzzanalysis.application.proposal.dto.ContentProposalDto;
import com.buzzanalysis.application.proposal.dto.ProposalGenerationRequest;
import com.buzzanalysis.domain.preprocessing.ContentFormat;
import com.buzzanalysis.domain.proposal.ContentProposal;
import com.buzzanalysis.domain.proposal.ContentProposalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProposalGenerationApplicationServiceTest {

    @Mock
    private CommonalityAnalysisApplicationService commonalityAnalysisApplicationService;
    @Mock
    private AiProposalGenerationPort aiProposalGenerationPort;
    @Mock
    private ContentProposalRepository contentProposalRepository;

    private ProposalGenerationApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ProposalGenerationApplicationService(commonalityAnalysisApplicationService,
                aiProposalGenerationPort, contentProposalRepository);
    }

    private CommonalityAnalysisResultDto sampleCommonality() {
        return new CommonalityAnalysisResultDto(10, 5, List.of(), 30, 20,
                ContentFormat.SHORT_VIDEO, "共通タイトル", "共通フック", "共通CTA", "共通構成", "共通ターゲット");
    }

    @Test
    void generate_defaultsToTwentyProposals_whenCountNotSpecified() {
        UUID postId = UUID.randomUUID();
        when(commonalityAnalysisApplicationService.analyze(List.of(postId))).thenReturn(sampleCommonality());
        when(aiProposalGenerationPort.generate(eq(sampleCommonality()), eq(20))).thenReturn(
                generatedProposals(20));
        when(contentProposalRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        List<ContentProposalDto> result = service.generate(new ProposalGenerationRequest(List.of(postId), null));

        assertThat(result).hasSize(20);
        assertThat(result.get(0).sequenceNumber()).isEqualTo(1);
        assertThat(result.get(19).sequenceNumber()).isEqualTo(20);
        UUID firstGenerationId = result.get(0).generationId();
        assertThat(result).allMatch(r -> r.generationId().equals(firstGenerationId));
    }

    @Test
    void generate_clampsRequestedCountToMax20() {
        UUID postId = UUID.randomUUID();
        when(commonalityAnalysisApplicationService.analyze(any())).thenReturn(sampleCommonality());
        when(aiProposalGenerationPort.generate(any(), eq(20))).thenReturn(generatedProposals(20));
        when(contentProposalRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        service.generate(new ProposalGenerationRequest(List.of(postId), 100));

        verify(aiProposalGenerationPort).generate(any(), eq(20));
    }

    @Test
    void generate_truncatesToRequestedCount_whenAiReturnsMore() {
        UUID postId = UUID.randomUUID();
        when(commonalityAnalysisApplicationService.analyze(any())).thenReturn(sampleCommonality());
        when(aiProposalGenerationPort.generate(any(), anyInt())).thenReturn(generatedProposals(20));
        when(contentProposalRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        List<ContentProposalDto> result = service.generate(new ProposalGenerationRequest(List.of(postId), 5));

        assertThat(result).hasSize(5);
    }

    @Test
    void findByGenerationId_returnsDtosFromRepository() {
        UUID generationId = UUID.randomUUID();
        ContentProposal proposal = ContentProposal.builder()
                .id(UUID.randomUUID()).generationId(generationId).sequenceNumber(1).title("企画")
                .createdAt(java.time.OffsetDateTime.now()).build();
        when(contentProposalRepository.findByGenerationId(generationId)).thenReturn(List.of(proposal));

        List<ContentProposalDto> result = service.findByGenerationId(generationId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("企画");
    }

    private List<AiProposalGenerationPort.GeneratedProposal> generatedProposals(int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(i -> new AiProposalGenerationPort.GeneratedProposal(
                        "企画" + i, "フック", "構成", "CTA", "ターゲット", "ジャンル", ContentFormat.SHORT_VIDEO, "理由"))
                .toList();
    }
}
