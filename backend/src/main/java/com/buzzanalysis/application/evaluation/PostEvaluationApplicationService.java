package com.buzzanalysis.application.evaluation;

import com.buzzanalysis.application.evaluation.dto.ContentEvaluationDto;
import com.buzzanalysis.application.evaluation.dto.PostEvaluationRequest;
import com.buzzanalysis.application.proposal.dto.ContentProposalDto;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.evaluation.ContentEvaluation;
import com.buzzanalysis.domain.evaluation.ContentEvaluationRepository;
import com.buzzanalysis.domain.proposal.ContentProposalRepository;
import com.buzzanalysis.infrastructure.quota.UsageQuotaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 「投稿評価」ユースケース（Phase14）。ユーザーが作成した（またはAIが生成した）台本・カルーセル等の
 * 投稿内容を評価する。{@code proposalId}が指定された場合のみPhase10の企画と比較し一致率を算出する
 * （未指定時は比較基準がないため一致率はnull）。
 */
@Service
public class PostEvaluationApplicationService {

    private final ContentProposalRepository contentProposalRepository;
    private final AiPostEvaluationPort aiPostEvaluationPort;
    private final ContentEvaluationRepository contentEvaluationRepository;
    private final UsageQuotaService usageQuotaService;

    public PostEvaluationApplicationService(ContentProposalRepository contentProposalRepository,
                                             AiPostEvaluationPort aiPostEvaluationPort,
                                             ContentEvaluationRepository contentEvaluationRepository,
                                             UsageQuotaService usageQuotaService) {
        this.contentProposalRepository = contentProposalRepository;
        this.aiPostEvaluationPort = aiPostEvaluationPort;
        this.contentEvaluationRepository = contentEvaluationRepository;
        this.usageQuotaService = usageQuotaService;
    }

    @Transactional
    public ContentEvaluationDto evaluate(PostEvaluationRequest request, UUID requestingUserId) {
        if (!usageQuotaService.tryConsume(requestingUserId)) {
            throw new BusinessRuleViolationException("本日のAI機能の利用回数上限に達しました。明日以降に再度お試しください。",
                    UsageQuotaService.EXCEEDED_ERROR_CODE);
        }
        Optional<ContentProposalDto> referenceProposal = Optional.empty();
        if (request.proposalId() != null) {
            ContentProposalDto proposalDto = contentProposalRepository.findById(request.proposalId())
                    .map(ContentProposalDto::from)
                    .orElseThrow(() -> EntityNotFoundException.of("ContentProposal", request.proposalId()));
            referenceProposal = Optional.of(proposalDto);
        }

        AiPostEvaluationPort.EvaluationTarget target = new AiPostEvaluationPort.EvaluationTarget(
                request.title(), request.hookText(), request.structureText(), request.ctaText(),
                request.targetAudienceText());
        AiPostEvaluationPort.AiEvaluationOutput output = aiPostEvaluationPort.evaluate(target, referenceProposal);

        ContentEvaluation evaluation = ContentEvaluation.builder()
                .id(UUID.randomUUID())
                .proposalId(request.proposalId())
                .title(request.title())
                .matchRatePercent(output.matchRatePercent())
                .targetAudienceEstimate(output.targetAudienceEstimate())
                .improvementSuggestions(output.improvementSuggestions())
                .hookImprovement(output.hookImprovement())
                .ctaImprovement(output.ctaImprovement())
                .predictedScore(output.predictedScore())
                .createdAt(OffsetDateTime.now())
                .build();

        return ContentEvaluationDto.from(contentEvaluationRepository.save(evaluation));
    }

    @Transactional(readOnly = true)
    public List<ContentEvaluationDto> findByProposalId(UUID proposalId) {
        return contentEvaluationRepository.findByProposalId(proposalId).stream()
                .map(ContentEvaluationDto::from)
                .toList();
    }
}
