package com.buzzanalysis.application.organization;

import com.buzzanalysis.application.organization.dto.OrganizationDto;
import com.buzzanalysis.application.organization.dto.OrganizationMemberDto;
import com.buzzanalysis.application.organization.dto.TeamSavedAnalysisDto;
import com.buzzanalysis.application.savedanalysis.SavedAnalysisApplicationService;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.organization.Organization;
import com.buzzanalysis.domain.organization.OrganizationMembership;
import com.buzzanalysis.domain.organization.OrganizationMembershipRepository;
import com.buzzanalysis.domain.organization.OrganizationRepository;
import com.buzzanalysis.domain.organization.OrganizationRole;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 組織(チーム)機能の土台となるユースケース。マルチアカウント/チーム機能の基盤設計
 * (改善計画No.24)として、組織の作成・メンバー招待/削除・チーム内での保存済み分析の
 * 共有閲覧を提供する。将来のプラン別の座席課金や組織単位のリソース分離(完全なマルチテナント化)は
 * このリポジトリの範囲外とし、ここでは「複数ユーザーが同じデータを見られる」という
 * 最小限の価値を成立させることに絞っている。
 */
@Service
public class OrganizationApplicationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final SavedAnalysisApplicationService savedAnalysisApplicationService;

    public OrganizationApplicationService(OrganizationRepository organizationRepository,
                                           OrganizationMembershipRepository membershipRepository,
                                           UserRepository userRepository,
                                           SavedAnalysisApplicationService savedAnalysisApplicationService) {
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.savedAnalysisApplicationService = savedAnalysisApplicationService;
    }

    /** 組織を作成し、作成者をOWNERとして登録する。 */
    @Transactional
    public OrganizationDto create(UUID creatorUserId, String name) {
        Organization organization = organizationRepository.save(Organization.createNew(name));
        membershipRepository.save(
                OrganizationMembership.createNew(organization.getId(), creatorUserId, OrganizationRole.OWNER));
        return OrganizationDto.from(organization, OrganizationRole.OWNER);
    }

    /** リクエストしたユーザーが所属する組織の一覧を返す。 */
    @Transactional(readOnly = true)
    public List<OrganizationDto> listMine(UUID userId) {
        List<OrganizationMembership> memberships = membershipRepository.findByUserId(userId);
        return memberships.stream()
                .map(m -> organizationRepository.findById(m.getOrganizationId())
                        .map(org -> OrganizationDto.from(org, m.getRole()))
                        .orElse(null))
                .filter(dto -> dto != null)
                .toList();
    }

    /** メールアドレスを指定して既存ユーザーを組織へ招待する。OWNERのみ実行可能。 */
    @Transactional
    public OrganizationMemberDto inviteMember(UUID organizationId, UUID requestingUserId, String email) {
        requireOwner(organizationId, requestingUserId);

        User invitee = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "指定のメールアドレスのユーザーが見つかりません。先にアカウント登録が必要です。"));

        if (membershipRepository.findByOrganizationIdAndUserId(organizationId, invitee.getId()).isPresent()) {
            throw new BusinessRuleViolationException("このユーザーは既にメンバーです");
        }

        OrganizationMembership membership = membershipRepository.save(
                OrganizationMembership.createNew(organizationId, invitee.getId(), OrganizationRole.MEMBER));
        return toMemberDto(membership, invitee);
    }

    /** 組織のメンバー一覧を返す。所属メンバーであれば誰でも閲覧可能。 */
    @Transactional(readOnly = true)
    public List<OrganizationMemberDto> listMembers(UUID organizationId, UUID requestingUserId) {
        requireMember(organizationId, requestingUserId);

        List<OrganizationMembership> memberships = membershipRepository.findByOrganizationId(organizationId);
        Map<UUID, User> usersByUserId = userRepository.findByIdIn(
                        memberships.stream().map(OrganizationMembership::getUserId).toList()).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return memberships.stream()
                .map(m -> {
                    User user = usersByUserId.get(m.getUserId());
                    return user == null ? null : toMemberDto(m, user);
                })
                .filter(dto -> dto != null)
                .toList();
    }

    /** メンバーを組織から削除する。OWNERのみ実行可能。組織に残る唯一のOWNERは削除できない。 */
    @Transactional
    public void removeMember(UUID organizationId, UUID requestingUserId, UUID targetUserId) {
        requireOwner(organizationId, requestingUserId);

        OrganizationMembership target = membershipRepository.findByOrganizationIdAndUserId(organizationId, targetUserId)
                .orElseThrow(() -> EntityNotFoundException.of("OrganizationMembership", targetUserId));

        if (target.getRole() == OrganizationRole.OWNER) {
            long ownerCount = membershipRepository.findByOrganizationId(organizationId).stream()
                    .filter(m -> m.getRole() == OrganizationRole.OWNER)
                    .count();
            if (ownerCount <= 1) {
                throw new BusinessRuleViolationException("組織に残る唯一のOWNERは削除できません");
            }
        }

        membershipRepository.delete(target);
    }

    /**
     * 組織メンバー全員の保存済み分析を集約して返す(チーム内でのブックマーク共有)。
     * 誰がいつ保存したかも含めて返すため、フロントエンドで「誰の分析か」を表示できる。
     */
    @Transactional(readOnly = true)
    public List<TeamSavedAnalysisDto> listTeamSavedAnalyses(UUID organizationId, UUID requestingUserId) {
        requireMember(organizationId, requestingUserId);

        List<OrganizationMembership> memberships = membershipRepository.findByOrganizationId(organizationId);
        Map<UUID, User> usersByUserId = userRepository.findByIdIn(
                        memberships.stream().map(OrganizationMembership::getUserId).toList()).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return memberships.stream()
                .flatMap(m -> {
                    User user = usersByUserId.get(m.getUserId());
                    if (user == null) {
                        return java.util.stream.Stream.empty();
                    }
                    return savedAnalysisApplicationService.list(m.getUserId()).stream()
                            .map(saved -> new TeamSavedAnalysisDto(saved, user.getId(), user.getDisplayName()));
                })
                .toList();
    }

    private OrganizationMembership requireMember(UUID organizationId, UUID userId) {
        return membershipRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new BusinessRuleViolationException("この組織のメンバーではありません"));
    }

    private void requireOwner(UUID organizationId, UUID userId) {
        OrganizationMembership membership = requireMember(organizationId, userId);
        if (membership.getRole() != OrganizationRole.OWNER) {
            throw new BusinessRuleViolationException("この操作にはOWNER権限が必要です");
        }
    }

    private OrganizationMemberDto toMemberDto(OrganizationMembership membership, User user) {
        return new OrganizationMemberDto(user.getId(), user.getEmail(), user.getDisplayName(),
                membership.getRole(), membership.getJoinedAt());
    }
}
