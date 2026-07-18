package com.buzzanalysis.application.script;

import com.buzzanalysis.application.proposal.dto.ContentProposalDto;
import com.buzzanalysis.application.script.dto.ScriptGenerationRequest;
import com.buzzanalysis.application.script.dto.VideoScriptDto;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.proposal.ContentProposal;
import com.buzzanalysis.domain.proposal.ContentProposalRepository;
import com.buzzanalysis.domain.script.ScriptCut;
import com.buzzanalysis.domain.script.VideoScript;
import com.buzzanalysis.domain.script.VideoScriptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 「台本生成」ユースケース（Phase11）。Phase10の投稿企画を入力に、指定した尺(30/60/90秒)の
 * ナレーション・テロップ・BGMイメージ・CTA・カット構成をAIが生成し、永続化する。
 */
@Service
public class ScriptGenerationApplicationService {

    private static final Set<Integer> ALLOWED_DURATIONS = Set.of(30, 60, 90);

    private final ContentProposalRepository contentProposalRepository;
    private final AiScriptGenerationPort aiScriptGenerationPort;
    private final VideoScriptRepository videoScriptRepository;

    public ScriptGenerationApplicationService(ContentProposalRepository contentProposalRepository,
                                               AiScriptGenerationPort aiScriptGenerationPort,
                                               VideoScriptRepository videoScriptRepository) {
        this.contentProposalRepository = contentProposalRepository;
        this.aiScriptGenerationPort = aiScriptGenerationPort;
        this.videoScriptRepository = videoScriptRepository;
    }

    @Transactional
    public VideoScriptDto generate(ScriptGenerationRequest request) {
        if (!ALLOWED_DURATIONS.contains(request.durationSeconds())) {
            throw new BusinessRuleViolationException("durationSecondsは30/60/90のいずれかである必要があります: "
                    + request.durationSeconds());
        }
        ContentProposal proposal = contentProposalRepository.findById(request.proposalId())
                .orElseThrow(() -> EntityNotFoundException.of("ContentProposal", request.proposalId()));

        AiScriptGenerationPort.GeneratedScript generated =
                aiScriptGenerationPort.generate(ContentProposalDto.from(proposal), request.durationSeconds());

        List<ScriptCut> cuts = generated.cuts().stream()
                .map(c -> new ScriptCut(c.cutNumber(), c.startSecond(), c.endSecond(), c.narration(), c.telop(),
                        c.visualDirection()))
                .toList();

        VideoScript script = VideoScript.builder()
                .id(UUID.randomUUID())
                .proposalId(request.proposalId())
                .durationSeconds(request.durationSeconds())
                .bgmImage(generated.bgmImage())
                .callToAction(generated.callToAction())
                .cuts(cuts)
                .createdAt(OffsetDateTime.now())
                .build();

        return VideoScriptDto.from(videoScriptRepository.save(script));
    }

    @Transactional(readOnly = true)
    public List<VideoScriptDto> findByProposalId(UUID proposalId) {
        return videoScriptRepository.findByProposalId(proposalId).stream()
                .map(VideoScriptDto::from)
                .toList();
    }
}
