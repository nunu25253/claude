package com.buzzanalysis.application.report.command;

import com.buzzanalysis.domain.report.ReportFormat;

/**
 * AIレポート生成をカプセル化するCommandインターフェース（Commandパターン）。
 * 実装（PDF/Markdown/HTML）はinfrastructure層に置き、それぞれ対応するライブラリでレンダリングを行う。
 */
public interface GenerateReportCommand {

    /** このCommandが担当する出力フォーマット。 */
    ReportFormat format();

    /** レポートを生成し、バイト列として返す。 */
    byte[] execute(ReportGenerationContext context);

    /** 生成物のMIMEタイプ（S3アップロード時のcontent-typeに使用）。 */
    String contentType();

    /** 生成物の拡張子（保存キーの決定に使用）。 */
    String fileExtension();
}
