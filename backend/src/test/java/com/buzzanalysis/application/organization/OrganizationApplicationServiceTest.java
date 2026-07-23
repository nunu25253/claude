package com.buzzanalysis.application.organization;

import com.buzzanalysis.application.organization.dto.OrganizationDto;
import com.buzzanalysis.application.organization.dto.OrganizationMemberDto;
import com.buzzanalysis.application.savedanalysis.SavedAnalysisApplicationService;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.organization.Organization;
import com.buzzanalysis.domain.organization.OrganizationMembership;
import com.buzzanalysis.domain.organization.OrganizationMembershipRepository;
import com.buzzanalysis.domain.organization.OrganizationRepository;
import com.buzzanalysis.domain.organization.OrganizationRole;
import com.buzzanalysis.domain.user.Role;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** {@link OrganizationApplicationService} の単体テスト。 */
@ExtendWith(MockitoExtension.class)
class OrganizationApplicationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private OrganizationMembershipRepository membershipRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SavedAnalysisApplicationService savedAnalysisApplicationService;

    private OrganizationApplicationService service;
    private UUID ownerId;
    private UUID organizationId;

    @BeforeEach
    void setUp() {
        service = new OrganizationApplicationService(organizationRepository, membershipRepository, userRepository,
                savedAnalysisApplicationService);
        ownerId = UUID.randomUUID();
        organizationId = UUID.randomUUID();
    }

    @Test
    void create_savesOrganizationAndOwnerMembership() {
        when(organizationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrganizationDto result = service.create(ownerId, "テストチーム");

        assertThat(result.name()).isEqualTo("テストチーム");
        assertThat(result.myRole()).isEqualTo(OrganizationRole.OWNER);
    }

    @Test
    void inviteMember_addsExistingUserAsMember_whenRequesterIsOwner() {
        UUID inviteeId = UUID.randomUUID();
        User invitee = new User(inviteeId, "invitee@example.com", "hash", "Invitee", Role.USER, true,
                OffsetDateTime.now(), OffsetDateTime.now());
        when(membershipRepository.findByOrganizationIdAndUserId(organizationId, ownerId)).thenReturn(Optional.of(
                new OrganizationMembership(UUID.randomUUID(), organizationId, ownerId, OrganizationRole.OWNER, OffsetDateTime.now())));
        when(userRepository.findByEmail("invitee@example.com")).thenReturn(Optional.of(invitee));
        when(membershipRepository.findByOrganizationIdAndUserId(organizationId, inviteeId)).thenReturn(Optional.empty());
        when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrganizationMemberDto result = service.inviteMember(organizationId, ownerId, "invitee@example.com");

        assertThat(result.userId()).isEqualTo(inviteeId);
        assertThat(result.role()).isEqualTo(OrganizationRole.MEMBER);
    }

    @Test
    void inviteMember_throwsBusinessRuleViolation_whenRequesterIsNotOwner() {
        when(membershipRepository.findByOrganizationIdAndUserId(organizationId, ownerId)).thenReturn(Optional.of(
                new OrganizationMembership(UUID.randomUUID(), organizationId, ownerId, OrganizationRole.MEMBER, OffsetDateTime.now())));

        assertThatThrownBy(() -> service.inviteMember(organizationId, ownerId, "invitee@example.com"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void inviteMember_throwsBusinessRuleViolation_whenInviteeDoesNotExist() {
        when(membershipRepository.findByOrganizationIdAndUserId(organizationId, ownerId)).thenReturn(Optional.of(
                new OrganizationMembership(UUID.randomUUID(), organizationId, ownerId, OrganizationRole.OWNER, OffsetDateTime.now())));
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.inviteMember(organizationId, ownerId, "nobody@example.com"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void inviteMember_throwsBusinessRuleViolation_whenAlreadyAMember() {
        UUID inviteeId = UUID.randomUUID();
        User invitee = new User(inviteeId, "invitee@example.com", "hash", "Invitee", Role.USER, true,
                OffsetDateTime.now(), OffsetDateTime.now());
        when(membershipRepository.findByOrganizationIdAndUserId(organizationId, ownerId)).thenReturn(Optional.of(
                new OrganizationMembership(UUID.randomUUID(), organizationId, ownerId, OrganizationRole.OWNER, OffsetDateTime.now())));
        when(userRepository.findByEmail("invitee@example.com")).thenReturn(Optional.of(invitee));
        when(membershipRepository.findByOrganizationIdAndUserId(organizationId, inviteeId)).thenReturn(Optional.of(
                new OrganizationMembership(UUID.randomUUID(), organizationId, inviteeId, OrganizationRole.MEMBER, OffsetDateTime.now())));

        assertThatThrownBy(() -> service.inviteMember(organizationId, ownerId, "invitee@example.com"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void removeMember_throwsBusinessRuleViolation_whenRemovingTheOnlyOwner() {
        when(membershipRepository.findByOrganizationIdAndUserId(organizationId, ownerId)).thenReturn(Optional.of(
                new OrganizationMembership(UUID.randomUUID(), organizationId, ownerId, OrganizationRole.OWNER, OffsetDateTime.now())));
        when(membershipRepository.findByOrganizationId(organizationId)).thenReturn(List.of(
                new OrganizationMembership(UUID.randomUUID(), organizationId, ownerId, OrganizationRole.OWNER, OffsetDateTime.now())));

        assertThatThrownBy(() -> service.removeMember(organizationId, ownerId, ownerId))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void removeMember_succeeds_whenAnotherOwnerRemainsOrTargetIsMember() {
        UUID memberId = UUID.randomUUID();
        OrganizationMembership ownerMembership =
                new OrganizationMembership(UUID.randomUUID(), organizationId, ownerId, OrganizationRole.OWNER, OffsetDateTime.now());
        OrganizationMembership memberMembership =
                new OrganizationMembership(UUID.randomUUID(), organizationId, memberId, OrganizationRole.MEMBER, OffsetDateTime.now());
        when(membershipRepository.findByOrganizationIdAndUserId(organizationId, ownerId)).thenReturn(Optional.of(ownerMembership));
        when(membershipRepository.findByOrganizationIdAndUserId(organizationId, memberId)).thenReturn(Optional.of(memberMembership));

        service.removeMember(organizationId, ownerId, memberId);

        org.mockito.Mockito.verify(membershipRepository).delete(memberMembership);
    }

    @Test
    void listTeamSavedAnalyses_requiresMembership() {
        when(membershipRepository.findByOrganizationIdAndUserId(organizationId, ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listTeamSavedAnalyses(organizationId, ownerId))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
