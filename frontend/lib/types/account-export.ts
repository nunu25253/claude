// バックエンドのAccountDataExportResponseに対応。個人情報保護法上の開示請求対応のための
// アカウントデータ一括ダウンロード。settings/subscriptionは未設定の場合null、
// 各項目内のnote/title/organizationName等は元々null許容のフィールドのためnullになりうる。
export interface AccountDataExport {
  exportedAt: string;
  profile: {
    userId: string;
    email: string;
    displayName: string | null;
    role: string;
    emailVerified: boolean;
    createdAt: string;
  };
  settings: {
    emailOnAnalysisComplete: boolean;
    emailWeeklyDigest: boolean;
    emailTrendingAlert: boolean;
    hasApiKey: boolean;
    apiKeyCreatedAt: string | null;
  } | null;
  savedAnalyses: {
    id: string;
    postId: string;
    note: string | null;
    createdAt: string;
    alertThreshold: number | null;
    alertTriggeredAt: string | null;
  }[];
  organizationMemberships: {
    organizationId: string;
    organizationName: string | null;
    role: string;
    joinedAt: string;
  }[];
  subscription: {
    plan: string;
    status: string;
    currentPeriodEnd: string | null;
    createdAt: string;
  } | null;
  reports: {
    id: string;
    postId: string;
    format: string;
    title: string | null;
    generatedAt: string;
  }[];
}
