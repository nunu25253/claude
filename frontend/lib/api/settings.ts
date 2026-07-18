import { apiClient } from "../api-client";
import type {
  ApiKeySettings,
  NotificationSettings,
  ProfileSettings,
} from "../types";

export const settingsApi = {
  getProfile: () => apiClient.get<ProfileSettings>("/settings/profile"),
  updateProfile: (payload: ProfileSettings) =>
    apiClient.put<ProfileSettings>("/settings/profile", payload),

  getNotifications: () =>
    apiClient.get<NotificationSettings>("/settings/notifications"),
  updateNotifications: (payload: NotificationSettings) =>
    apiClient.put<NotificationSettings>("/settings/notifications", payload),

  getApiKey: () => apiClient.get<ApiKeySettings>("/settings/api-key"),
  regenerateApiKey: () =>
    apiClient.post<ApiKeySettings>("/settings/api-key/regenerate"),
};
