package com.buzzanalysis.application.auth;

import com.buzzanalysis.domain.billing.Subscription;
import com.buzzanalysis.domain.billing.SubscriptionPlan;
import com.buzzanalysis.domain.billing.SubscriptionRepository;
import com.buzzanalysis.domain.billing.SubscriptionStatus;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.organization.Organization;
import com.buzzanalysis.domain.organization.OrganizationMembership;
import com.buzzanalysis.domain.organization.OrganizationMembershipRepository;
import com.buzzanalysis.domain.organization.OrganizationRepository;
import com.buzzanalysis.domain.organization.OrganizationRole;
import com.buzzanalysis.domain.report.Report;
import com.buzzanalysis.domain.report.ReportFormat;
import com.buzzanalysis.domain.report.ReportRepository;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysis;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysisRepository;
import com.buzzanalysis.domain.settings.UserSettings;
import com.buzzanalysis.domain.settings.UserSettingsRepository;
import com.buzzanalysis.domain.user.Role;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import com.buzzanalysis.presentation.dto.response.AccountDataExportResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * {@link AccountDataExportApplicationService} のMockitoによる単体テスト。
 * 各リポジトリはすべてモック化し、複数ドメインの集計結果が正しくレスポンスへ写像されることを検証する。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountDataExportApplicationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserSettingsRepository userSettingsRepository;
    @Mock
    private SavedAnalysisRepository savedAnalysisRepository;
    @Mock
    private OrganizationMembershipRepository organizationMembershipRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private ReportRepository reportRepository;

    private AccountDataExportApplicationService service;
    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        service = new AccountDataExportApplicationService(userRepository, userSettingsRepository,
                savedAnalysisRepository, organizationMembershipRepository, organizationRepository,
                subscriptionRepository, reportRepository);
        userId = UUID.randomUUID();
        user = new User(userId, "user@example.com", "hashed", "Test User", Role.USER, true,
                OffsetDateTime.now(), OffsetDateTime.now());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    }

    @Test
    void export_throwsEntityNotFound_whenUserDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.export(unknownId)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void export_includesProfileWithoutPasswordHash() {
        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(savedAnalysisRepository.findByUserId(userId)).thenReturn(List.of());
        when(organizationMembershipRepository.findByUserId(userId)).thenReturn(List.of());
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(reportRepository.findByUserIdOrderByGeneratedAtDesc(userId)).thenReturn(List.of());

        AccountDataExportResponse result = service.export(userId);

        assertThat(result.profile().userId()).isEqualTo(userId);
        assertThat(result.profile().email()).isEqualTo("user@example.com");
        assertThat(result.profile().displayName()).isEqualTo("Test User");
        assertThat(result.profile().role()).isEqualTo("USER");
        assertThat(result.profile().emailVerified()).isTrue();
        assertThat(result.settings()).isNull();
        assertThat(result.savedAnalyses()).isEmpty();
        assertThat(result.organizationMemberships()).isEmpty();
        assertThat(result.subscription()).isNull();
        assertThat(result.reports()).isEmpty();
    }

    @Test
    void export_masksApiKeyValue_butIndicatesItExists() {
        OffsetDateTime apiKeyCreatedAt = OffsetDateTime.now();
        UserSettings settings = new UserSettings(userId, true, false, true, "super-secret-api-key",
                apiKeyCreatedAt, OffsetDateTime.now(), OffsetDateTime.now());
        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(savedAnalysisRepository.findByUserId(userId)).thenReturn(List.of());
        when(organizationMembershipRepository.findByUserId(userId)).thenReturn(List.of());
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(reportRepository.findByUserIdOrderByGeneratedAtDesc(userId)).thenReturn(List.of());

        AccountDataExportResponse result = service.export(userId);

        assertThat(result.settings().hasApiKey()).isTrue();
        assertThat(result.settings().apiKeyCreatedAt()).isEqualTo(apiKeyCreatedAt);
        assertThat(result.settings().emailWeeklyDigest()).isFalse();
        assertThat(result.settings().emailTrendingAlert()).isTrue();
    }

    @Test
    void export_resolvesOrganizationNameForEachMembership() {
        UUID orgId = UUID.randomUUID();
        OrganizationMembership membership = new OrganizationMembership(UUID.randomUUID(), orgId, userId,
                OrganizationRole.OWNER, OffsetDateTime.now());
        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(savedAnalysisRepository.findByUserId(userId)).thenReturn(List.of());
        when(organizationMembershipRepository.findByUserId(userId)).thenReturn(List.of(membership));
        when(organizationRepository.findById(orgId))
                .thenReturn(Optional.of(new Organization(orgId, "テストチーム", OffsetDateTime.now())));
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(reportRepository.findByUserIdOrderByGeneratedAtDesc(userId)).thenReturn(List.of());

        AccountDataExportResponse result = service.export(userId);

        assertThat(result.organizationMemberships()).hasSize(1);
        assertThat(result.organizationMemberships().get(0).organizationName()).isEqualTo("テストチーム");
        assertThat(result.organizationMemberships().get(0).role()).isEqualTo("OWNER");
    }

    @Test
    void export_includesSavedAnalysesSubscriptionAndReports() {
        SavedAnalysis savedAnalysis = SavedAnalysis.createNew(userId, UUID.randomUUID(), "メモ");
        Subscription subscription = new Subscription(UUID.randomUUID(), userId, SubscriptionPlan.PRO,
                SubscriptionStatus.ACTIVE, "member-123", OffsetDateTime.now().plusDays(30),
                OffsetDateTime.now(), OffsetDateTime.now());
        Report report = Report.builder()
                .postId(UUID.randomUUID())
                .userId(userId)
                .format(ReportFormat.PDF)
                .title("週次レポート")
                .storageKey("reports/some-key.pdf")
                .contentSizeBytes(1024)
                .build();
        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(savedAnalysisRepository.findByUserId(userId)).thenReturn(List.of(savedAnalysis));
        when(organizationMembershipRepository.findByUserId(userId)).thenReturn(List.of());
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.of(subscription));
        when(reportRepository.findByUserIdOrderByGeneratedAtDesc(userId)).thenReturn(List.of(report));

        AccountDataExportResponse result = service.export(userId);

        assertThat(result.savedAnalyses()).hasSize(1);
        assertThat(result.savedAnalyses().get(0).note()).isEqualTo("メモ");
        assertThat(result.subscription().plan()).isEqualTo("PRO");
        assertThat(result.subscription().status()).isEqualTo("ACTIVE");
        assertThat(result.reports()).hasSize(1);
        assertThat(result.reports().get(0).title()).isEqualTo("週次レポート");
        assertThat(result.reports().get(0).format()).isEqualTo("PDF");
    }

    @Test
    void export_setsExportedAtToApproximatelyNow() {
        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(savedAnalysisRepository.findByUserId(userId)).thenReturn(List.of());
        when(organizationMembershipRepository.findByUserId(userId)).thenReturn(List.of());
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(reportRepository.findByUserIdOrderByGeneratedAtDesc(userId)).thenReturn(List.of());

        OffsetDateTime before = OffsetDateTime.now().minusSeconds(1);
        AccountDataExportResponse result = service.export(userId);
        OffsetDateTime after = OffsetDateTime.now().plusSeconds(1);

        assertThat(result.exportedAt()).isBetween(before, after);
        // organizationRepositoryはメンバーシップが無い場合は一切呼ばれない
        org.mockito.Mockito.verifyNoInteractions(organizationRepository);
    }
}
