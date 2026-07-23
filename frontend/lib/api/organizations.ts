import { apiClient } from "../api-client";
import type {
  CreateOrganizationRequest,
  InviteMemberRequest,
  Organization,
  OrganizationMember,
  TeamSavedAnalysis,
} from "../types";

export const organizationsApi = {
  listMine: () => apiClient.get<Organization[]>("/organizations/mine"),

  create: (payload: CreateOrganizationRequest) =>
    apiClient.post<Organization>("/organizations", payload),

  listMembers: (organizationId: string) =>
    apiClient.get<OrganizationMember[]>(`/organizations/${organizationId}/members`),

  inviteMember: (organizationId: string, payload: InviteMemberRequest) =>
    apiClient.post<OrganizationMember>(`/organizations/${organizationId}/members`, payload),

  removeMember: (organizationId: string, userId: string) =>
    apiClient.delete<void>(`/organizations/${organizationId}/members/${userId}`),

  listTeamSavedAnalyses: (organizationId: string) =>
    apiClient.get<TeamSavedAnalysis[]>(`/organizations/${organizationId}/saved-analyses`),
};
