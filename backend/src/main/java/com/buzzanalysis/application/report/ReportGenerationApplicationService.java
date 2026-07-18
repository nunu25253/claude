package com.buzzanalysis.application.report;

import com.buzzanalysis.application.report.command.GenerateReportCommand;
import com.buzzanalysis.application.report.command.ReportGenerationContext;
import com.buzzanalysis.application.report.dto.ReportDto;
import com.buzzanalysis.application.report.dto.ReportHistoryItemDto;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.report.Report;
import com.buzzanalysis.domain.report.ReportFormat;
import com.buzzanalysis.domain.report.ReportRepository;
import com.buzzanalysis.domain.score.BuzzScore;
import com.buzzanalysis.domain.score.BuzzScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AIレポート生成ユースケース。フォーマット（PDF/Markdown/HTML）ごとに実装が異なる生成処理を
 * {@link GenerateReportCommand}（Commandパターン）に委譲し、生成物をS3互換ストレージに保存する。
 */
@Service
public class ReportGenerationApplicationService {

    private final PostRepository postRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final BuzzScoreRepository buzzScoreRepository;
    private final ReportRepository reportRepository;
    private final StoragePort storagePort;
    private final Map<ReportFormat, GenerateReportCommand> commandsByFormat;

    public ReportGenerationApplicationService(PostRepository postRepository,
                                               AnalysisResultRepository analysisResultRepository,
                                               BuzzScoreRepository buzzScoreRepository,
                                               ReportRepository reportRepository,
                                               StoragePort storagePort,
                                               List<GenerateReportCommand> commands) {
        this.postRepository = postRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.buzzScoreRepository = buzzScoreRepository;
        this.reportRepository = reportRepository;
        this.storagePort = storagePort;
        this.commandsByFormat = commands.stream()
                .collect(Collectors.toMap(GenerateReportCommand::format, Function.identity()));
    }

    @Transactional
    public ReportDto generateReport(UUID userId, UUID postId, ReportFormat format) {
        GenerateReportCommand command = commandsByFormat.get(format);
        if (command == null) {
            throw new BusinessRuleViolationException("Unsupported report format: " + format);
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> EntityNotFoundException.of("Post", postId));
        AnalysisResult analysisResult = analysisResultRepository.findByPostId(postId)
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "Post has not been analyzed yet. Call POST /api/v1/posts/analyze first: postId=" + postId));
        BuzzScore buzzScore = buzzScoreRepository.findByPostId(postId)
                .orElseThrow(() -> new BusinessRuleViolationException("BuzzScore not calculated yet: postId=" + postId));

        byte[] content = command.execute(new ReportGenerationContext(post, analysisResult, buzzScore));

        String key = "reports/%s/%s.%s".formatted(postId, UUID.randomUUID(), command.fileExtension());
        storagePort.upload(key, content, command.contentType());

        Report report = Report.builder()
                .postId(postId)
                .userId(userId)
                .format(format)
                .title("Buzz Analysis Report - " + post.getAuthorName())
                .storageKey(key)
                .contentSizeBytes(content.length)
                .build();
        Report saved = reportRepository.save(report);

        String downloadUrl = storagePort.generateAccessUrl(key);
        return ReportDto.from(saved, downloadUrl);
    }

    /** ログイン中ユーザーが生成したレポートの履歴一覧（新しい順）。 */
    @Transactional(readOnly = true)
    public List<ReportHistoryItemDto> getHistory(UUID userId) {
        List<Report> reports = reportRepository.findByUserIdOrderByGeneratedAtDesc(userId);
        List<ReportHistoryItemDto> history = new ArrayList<>();
        for (Report report : reports) {
            Optional<Post> post = postRepository.findById(report.getPostId());
            String caption = post.map(Post::getCaption).orElse(null);
            String downloadUrl = storagePort.generateAccessUrl(report.getStorageKey());
            history.add(ReportHistoryItemDto.from(report, caption, downloadUrl));
        }
        return history;
    }
}
