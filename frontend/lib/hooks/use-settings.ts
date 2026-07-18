import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { settingsApi } from "@/lib/api";
import type { NotificationSettings, ProfileSettings } from "@/lib/types";

export function useProfileSettings() {
  return useQuery({
    queryKey: ["settings", "profile"],
    queryFn: () => settingsApi.getProfile(),
  });
}

export function useUpdateProfileSettings() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: ProfileSettings) => settingsApi.updateProfile(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["settings", "profile"] }),
  });
}

export function useNotificationSettings() {
  return useQuery({
    queryKey: ["settings", "notifications"],
    queryFn: () => settingsApi.getNotifications(),
  });
}

export function useUpdateNotificationSettings() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: NotificationSettings) => settingsApi.updateNotifications(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["settings", "notifications"] }),
  });
}

export function useApiKeySettings() {
  return useQuery({
    queryKey: ["settings", "api-key"],
    queryFn: () => settingsApi.getApiKey(),
  });
}

export function useRegenerateApiKey() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => settingsApi.regenerateApiKey(),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["settings", "api-key"] }),
  });
}
