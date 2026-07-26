package com.buzzanalysis.presentation.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 個人情報保護法上の開示請求対応のためのアカウントデータエクスポート。
 * アカウント削除(V24__account_deletion_cascades.sql)でユーザーに紐づくとみなしているデータ範囲
 * (プロフィール・通知設定・保存済み分析・チーム所属・購読・レポート履歴)と対になるよう、
 * 「削除されうるデータは事前にエクスポートできる」を満たす範囲で構成する。
 * パスワードハッシュやAPIキーの値そのものなど、漏洩リスクのある秘匿情報は含めない。
 */
public record AccountDataExportResponse(
        OffsetDateTime exportedAt,
        ProfileExport profile,
        SettingsExport settings,
        List<SavedAnalysisExport> savedAnalyses,
        List<OrganizationMembershipExport> organizationMemberships,
        SubscriptionExport subscription,
        List<ReportExport> reports
) {

    public record ProfileExport(
            UUID userId,
            String email,
            String displayName,
            String role,
            boolean emailVerified,
            OffsetDateTime createdAt
    ) {
    }

    /**
     * apiKey/Slack Webhook URLの値自体は漏洩リスクがあるため含めず、設定有無(hasApiKey/hasSlackWebhook)
     * のみ示す。
     */
    public record SettingsExport(
            boolean emailOnAnalysisComplete,
            boolean emailWeeklyDigest,
            boolean emailTrendingAlert,
            boolean hasApiKey,
            OffsetDateTime apiKeyCreatedAt,
            boolean hasSlackWebhook
    ) {
    }

    public record SavedAnalysisExport(
            UUID id,
            UUID postId,
            String note,
            OffsetDateTime createdAt,
            Double alertThreshold,
            OffsetDateTime alertTriggeredAt
    ) {
    }

    public record OrganizationMembershipExport(
            UUID organizationId,
            String organizationName,
            String role,
            OffsetDateTime joinedAt
    ) {
    }

    public record SubscriptionExport(
            String plan,
            String status,
            OffsetDateTime currentPeriodEnd,
            OffsetDateTime createdAt
    ) {
    }

    public record ReportExport(
            UUID id,
            UUID postId,
            String format,
            String title,
            OffsetDateTime generatedAt
    ) {
    }
}
