package com.buzzanalysis.application.proposal;

import com.buzzanalysis.application.commonality.CommonalityAnalysisApplicationService;
import com.buzzanalysis.application.commonality.dto.CommonalityAnalysisResultDto;
import com.buzzanalysis.application.proposal.dto.ContentProposalDto;
import com.buzzanalysis.application.proposal.dto.ProposalGenerationRequest;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.proposal.ContentProposal;
import com.buzzanalysis.domain.proposal.ContentProposalRepository;
import com.buzzanalysis.infrastructure.quota.UsageQuotaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 「企画生成」ユースケース（Phase10）。Phase8の共通点分析結果を元に、AIが投稿企画を
 * 複数件（既定20件）生成し、同一{@code generationId}でグルーピングして永続化する。
 */
@Service
public class ProposalGenerationApplicationService {

    private static final int DEFAULT_COUNT = 20;
    private static final int MAX_COUNT = 20;

    private final CommonalityAnalysisApplicationService commonalityAnalysisApplicationService;
    private final AiProposalGenerationPort aiProposalGenerationPort;
    private final ContentProposalRepository contentProposalRepository;
    private final UsageQuotaService usageQuotaService;

    public ProposalGenerationApplicationService(CommonalityAnalysisApplicationService commonalityAnalysisApplicationService,
                                                 AiProposalGenerationPort aiProposalGenerationPort,
                                                 ContentProposalRepository contentProposalRepository,
                                                 UsageQuotaService usageQuotaService) {
        this.commonalityAnalysisApplicationService = commonalityAnalysisApplicationService;
        this.aiProposalGenerationPort = aiProposalGenerationPort;
        this.contentProposalRepository = contentProposalRepository;
        this.usageQuotaService = usageQuotaService;
    }

    @Transactional
    public List<ContentProposalDto> generate(ProposalGenerationRequest request, UUID requestingUserId) {
        if (!usageQuotaService.tryConsume(requestingUserId)) {
            throw new BusinessRuleViolationException("本日のAI機能の利用回数上限に達しました。明日以降に再度お試しください。");
        }
        int requestedCount = resolveCount(request.count());
        CommonalityAnalysisResultDto commonality =
                commonalityAnalysisApplicationService.analyze(request.postIds(), requestingUserId);
        List<AiProposalGenerationPort.GeneratedProposal> generated =
                aiProposalGenerationPort.generate(commonality, requestedCount);

        UUID generationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        List<ContentProposal> proposals = new ArrayList<>();
        int sequence = 1;
        for (AiProposalGenerationPort.GeneratedProposal g : generated) {
            if (sequence > requestedCount) {
                break;
            }
            proposals.add(ContentProposal.builder()
                    .id(UUID.randomUUID())
                    .generationId(generationId)
                    .sequenceNumber(sequence)
                    .title(g.title())
                    .hookPattern(g.hookPattern())
                    .structureSummary(g.structureSummary())
                    .callToAction(g.callToAction())
                    .targetAudience(g.targetAudience())
                    .genre(g.genre())
                    .recommendedFormat(g.recommendedFormat())
                    .reasoning(g.reasoning())
                    .createdAt(now)
                    .build());
            sequence++;
        }

        List<ContentProposal> saved = contentProposalRepository.saveAll(proposals);
        return saved.stream().map(ContentProposalDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ContentProposalDto> findByGenerationId(UUID generationId) {
        return contentProposalRepository.findByGenerationId(generationId).stream()
                .map(ContentProposalDto::from)
                .toList();
    }

    private int resolveCount(Integer requested) {
        if (requested == null || requested <= 0) {
            return DEFAULT_COUNT;
        }
        return Math.min(requested, MAX_COUNT);
    }
}
