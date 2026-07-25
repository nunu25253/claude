package com.buzzanalysis.application.report;

import com.buzzanalysis.application.report.command.GenerateReportCommand;
import com.buzzanalysis.application.report.command.ReportGenerationContext;
import com.buzzanalysis.application.report.dto.ReportDto;
import com.buzzanalysis.application.report.dto.ReportHistoryItemDto;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.report.Report;
import com.buzzanalysis.domain.report.ReportFormat;
import com.buzzanalysis.domain.report.ReportRepository;
import com.buzzanalysis.domain.score.BuzzScore;
import com.buzzanalysis.domain.score.BuzzScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportGenerationApplicationServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private AnalysisResultRepository analysisResultRepository;
    @Mock
    private BuzzScoreRepository buzzScoreRepository;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private StoragePort storagePort;
    @Mock
    private GenerateReportCommand markdownCommand;

    private ReportGenerationApplicationService service;
    private UUID userId;
    private UUID postId;

    @BeforeEach
    void setUp() {
        when(markdownCommand.format()).thenReturn(ReportFormat.MARKDOWN);
        service = new ReportGenerationApplicationService(postRepository, analysisResultRepository,
                buzzScoreRepository, reportRepository, storagePort, List.of(markdownCommand));
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
    }

    @Test
    void generateReport_savesReport_withRequestingUserId() {
        Post post = post(postId);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(analysisResultRepository.findByPostId(postId))
                .thenReturn(Optional.of(AnalysisResult.builder().postId(postId).genre("美容").build()));
        when(buzzScoreRepository.findByPostId(postId))
                .thenReturn(Optional.of(BuzzScore.of(postId, 80.0, Map.of())));
        when(markdownCommand.execute(any())).thenReturn("content".getBytes());
        when(markdownCommand.contentType()).thenReturn("text/markdown");
        when(markdownCommand.fileExtension()).thenReturn("md");
        when(storagePort.generateAccessUrl(any())).thenReturn("https://storage.example.com/x");
        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        when(reportRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        ReportDto result = service.generateReport(userId, postId, ReportFormat.MARKDOWN);

        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(result.downloadUrl()).isEqualTo("https://storage.example.com/x");
    }

    @Test
    void getHistory_returnsReportsNewestFirst_withPostCaptionAndDownloadUrl() {
        Report report = Report.builder().postId(postId).userId(userId).format(ReportFormat.MARKDOWN)
                .title("t").storageKey("reports/x.md").contentSizeBytes(10).build();
        when(reportRepository.findByUserIdOrderByGeneratedAtDesc(userId)).thenReturn(List.of(report));
        when(postRepository.findByIdIn(any())).thenReturn(List.of(post(postId)));
        when(storagePort.generateAccessUrl("reports/x.md")).thenReturn("https://storage.example.com/x.md");

        List<ReportHistoryItemDto> result = service.getHistory(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).postId()).isEqualTo(postId);
        assertThat(result.get(0).postCaption()).isEqualTo("caption");
        assertThat(result.get(0).downloadUrl()).isEqualTo("https://storage.example.com/x.md");
    }

    @Test
    void getHistory_returnsNullCaption_whenPostWasDeleted() {
        Report report = Report.builder().postId(postId).userId(userId).format(ReportFormat.PDF)
                .title("t").storageKey("reports/y.pdf").contentSizeBytes(10).build();
        when(reportRepository.findByUserIdOrderByGeneratedAtDesc(userId)).thenReturn(List.of(report));
        when(postRepository.findByIdIn(any())).thenReturn(List.of());
        when(storagePort.generateAccessUrl(any())).thenReturn("https://storage.example.com/y.pdf");

        List<ReportHistoryItemDto> result = service.getHistory(userId);

        assertThat(result.get(0).postCaption()).isNull();
    }

    private Post post(UUID id) {
        return new Post(id, UUID.randomUUID(), Platform.INSTAGRAM, "ext-" + id, "https://instagram.com/p/x",
                OffsetDateTime.now(), "creator", "caption", List.of(), 100L, 10L, null, null, null, 1,
                PostType.CAROUSEL, OffsetDateTime.now(), OffsetDateTime.now());
    }
}
