package com.buzzanalysis.infrastructure.report;

import com.buzzanalysis.application.report.command.GenerateReportCommand;
import com.buzzanalysis.application.report.command.ReportGenerationContext;
import com.buzzanalysis.domain.report.ReportFormat;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/** HTML形式でAIレポートを生成するCommand実装。 */
@Component
public class HtmlReportCommand implements GenerateReportCommand {

    private final ReportContentBuilder contentBuilder;

    public HtmlReportCommand(ReportContentBuilder contentBuilder) {
        this.contentBuilder = contentBuilder;
    }

    @Override
    public ReportFormat format() {
        return ReportFormat.HTML;
    }

    @Override
    public byte[] execute(ReportGenerationContext context) {
        return contentBuilder.buildHtml(context).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String contentType() {
        return "text/html; charset=UTF-8";
    }

    @Override
    public String fileExtension() {
        return "html";
    }
}
