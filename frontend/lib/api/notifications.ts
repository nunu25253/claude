import { apiClient } from "../api-client";
import type { Notification, UnreadNotificationCount } from "../types";

export const notificationsApi = {
  list: () => apiClient.get<Notification[]>("/notifications"),

  unreadCount: () => apiClient.get<UnreadNotificationCount>("/notifications/unread-count"),

  markRead: (id: string) => apiClient.post<void>(`/notifications/${id}/read`),

  markAllRead: () => apiClient.post<void>("/notifications/read-all"),
};
