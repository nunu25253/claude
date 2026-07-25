package com.buzzanalysis.application.auth;

import com.buzzanalysis.domain.billing.SubscriptionRepository;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.organization.Organization;
import com.buzzanalysis.domain.organization.OrganizationMembershipRepository;
import com.buzzanalysis.domain.organization.OrganizationRepository;
import com.buzzanalysis.domain.report.ReportRepository;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysisRepository;
import com.buzzanalysis.domain.settings.UserSettingsRepository;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import com.buzzanalysis.presentation.dto.response.AccountDataExportResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 個人情報保護法上の開示請求対応(ユーザー自身のデータをダウンロード可能な形で取得できる権利)の
 * ためのアプリケーションサービス。アカウント削除機能とは対になる関係にあり、削除時に
 * 一緒に消えるデータ範囲を、削除前にユーザー自身が確認・保管できるようにする。
 */
@Service
public class AccountDataExportApplicationService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final SavedAnalysisRepository savedAnalysisRepository;
    private final OrganizationMembershipRepository organizationMembershipRepository;
    private final OrganizationRepository organizationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ReportRepository reportRepository;

    public AccountDataExportApplicationService(UserRepository userRepository,
                                                UserSettingsRepository userSettingsRepository,
                                                SavedAnalysisRepository savedAnalysisRepository,
                                                OrganizationMembershipRepository organizationMembershipRepository,
                                                OrganizationRepository organizationRepository,
                                                SubscriptionRepository subscriptionRepository,
                                                ReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.savedAnalysisRepository = savedAnalysisRepository;
        this.organizationMembershipRepository = organizationMembershipRepository;
        this.organizationRepository = organizationRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional(readOnly = true)
    public AccountDataExportResponse export(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> EntityNotFoundException.of("User", userId));

        var profile = new AccountDataExportResponse.ProfileExport(
                user.getId(), user.getEmail(), user.getDisplayName(),
                user.getRole().name(), user.isEmailVerified(), user.getCreatedAt());

        var settings = userSettingsRepository.findByUserId(userId)
                .map(s -> new AccountDataExportResponse.SettingsExport(
                        s.isEmailOnAnalysisComplete(), s.isEmailWeeklyDigest(), s.isEmailTrendingAlert(),
                        s.getApiKey() != null, s.getApiKeyCreatedAt()))
                .orElse(null);

        var savedAnalyses = savedAnalysisRepository.findByUserId(userId).stream()
                .map(sa -> new AccountDataExportResponse.SavedAnalysisExport(
                        sa.getId(), sa.getPostId(), sa.getNote(), sa.getCreatedAt(),
                        sa.getAlertThreshold(), sa.getAlertTriggeredAt()))
                .toList();

        var memberships = organizationMembershipRepository.findByUserId(userId).stream()
                .map(m -> new AccountDataExportResponse.OrganizationMembershipExport(
                        m.getOrganizationId(),
                        organizationRepository.findById(m.getOrganizationId())
                                .map(Organization::getName)
                                .orElse(null),
                        m.getRole().name(), m.getJoinedAt()))
                .toList();

        var subscription = subscriptionRepository.findByUserId(userId)
                .map(s -> new AccountDataExportResponse.SubscriptionExport(
                        s.getPlan().name(), s.getStatus().name(), s.getCurrentPeriodEnd(), s.getCreatedAt()))
                .orElse(null);

        var reports = reportRepository.findByUserIdOrderByGeneratedAtDesc(userId).stream()
                .map(r -> new AccountDataExportResponse.ReportExport(
                        r.getId(), r.getPostId(), r.getFormat().name(), r.getTitle(), r.getGeneratedAt()))
                .toList();

        return new AccountDataExportResponse(
                OffsetDateTime.now(), profile, settings, savedAnalyses, memberships, subscription, reports);
    }
}
