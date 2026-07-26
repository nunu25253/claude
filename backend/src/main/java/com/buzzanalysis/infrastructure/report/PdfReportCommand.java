package com.buzzanalysis.infrastructure.report;

import com.buzzanalysis.application.report.command.GenerateReportCommand;
import com.buzzanalysis.application.report.command.ReportGenerationContext;
import com.buzzanalysis.domain.common.exception.ExternalApiException;
import com.buzzanalysis.domain.report.ReportFormat;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

/**
 * PDF形式でAIレポートを生成するCommand実装。HTMLをopenhtmltopdfでレンダリングする。
 * 注意: 日本語を正しく表示するには本番運用時にCJK対応フォント（例: Noto Sans JP）を
 * {@code PdfRendererBuilder#useFont(...)} で埋め込む必要がある。
 */
@Component
public class PdfReportCommand implements GenerateReportCommand {

    private final ReportContentBuilder contentBuilder;

    public PdfReportCommand(ReportContentBuilder contentBuilder) {
        this.contentBuilder = contentBuilder;
    }

    @Override
    public ReportFormat format() {
        return ReportFormat.PDF;
    }

    @Override
    public byte[] execute(ReportGenerationContext context) {
        String html = contentBuilder.buildHtml(context);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new ExternalApiException("Failed to render PDF report", e);
        }
    }

    @Override
    public String contentType() {
        return "application/pdf";
    }

    @Override
    public String fileExtension() {
        return "pdf";
    }
}
