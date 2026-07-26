export type ReportFormat = "pdf" | "markdown" | "html";

export interface GenerateReportRequest {
  postId: string;
  format: ReportFormat;
}

// レポート生成結果。バックエンドがファイルを直接返す場合と
// ダウンロードURLを返す場合の両方に対応できるようにしておく
export interface GeneratedReport {
  reportId: string;
  postId: string;
  format: ReportFormat;
  downloadUrl?: string;
  fileName: string;
  createdAt: string;
}

export interface ReportHistoryItem {
  reportId: string;
  postId: string;
  postCaption: string;
  format: ReportFormat;
  createdAt: string;
  downloadUrl?: string;
}
