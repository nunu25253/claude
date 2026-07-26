package com.buzzanalysis.infrastructure.report;

import com.buzzanalysis.application.report.command.GenerateReportCommand;
import com.buzzanalysis.application.report.command.ReportGenerationContext;
import com.buzzanalysis.domain.report.ReportFormat;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/** Markdown形式でAIレポートを生成するCommand実装。 */
@Component
public class MarkdownReportCommand implements GenerateReportCommand {

    private final ReportContentBuilder contentBuilder;

    public MarkdownReportCommand(ReportContentBuilder contentBuilder) {
        this.contentBuilder = contentBuilder;
    }

    @Override
    public ReportFormat format() {
        return ReportFormat.MARKDOWN;
    }

    @Override
    public byte[] execute(ReportGenerationContext context) {
        return contentBuilder.buildMarkdown(context).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String contentType() {
        return "text/markdown; charset=UTF-8";
    }

    @Override
    public String fileExtension() {
        return "md";
    }
}
