import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { organizationsApi } from "@/lib/api";
import type { CreateOrganizationRequest, InviteMemberRequest } from "@/lib/types";

const ORGANIZATIONS_KEY = ["organizations"];
const membersKey = (organizationId: string) => ["organizations", organizationId, "members"];
const teamSavedAnalysesKey = (organizationId: string) => [
  "organizations",
  organizationId,
  "saved-analyses",
];

export function useOrganizations() {
  return useQuery({
    queryKey: ORGANIZATIONS_KEY,
    queryFn: () => organizationsApi.listMine(),
  });
}

export function useCreateOrganization() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: CreateOrganizationRequest) => organizationsApi.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ORGANIZATIONS_KEY });
    },
  });
}

export function useOrganizationMembers(organizationId: string | undefined) {
  return useQuery({
    queryKey: membersKey(organizationId ?? ""),
    queryFn: () => organizationsApi.listMembers(organizationId as string),
    enabled: !!organizationId,
  });
}

export function useInviteMember(organizationId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: InviteMemberRequest) => organizationsApi.inviteMember(organizationId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: membersKey(organizationId) });
    },
  });
}

export function useRemoveMember(organizationId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (userId: string) => organizationsApi.removeMember(organizationId, userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: membersKey(organizationId) });
    },
  });
}

export function useTeamSavedAnalyses(organizationId: string | undefined) {
  return useQuery({
    queryKey: teamSavedAnalysesKey(organizationId ?? ""),
    queryFn: () => organizationsApi.listTeamSavedAnalyses(organizationId as string),
    enabled: !!organizationId,
  });
}
