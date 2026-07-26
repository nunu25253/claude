/** アプリ内通知センター関連の型定義。 */

export type NotificationType = "THRESHOLD_ALERT" | "WEEKLY_DIGEST";

export interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  body: string;
  link?: string;
  createdAt: string;
  unread: boolean;
}

export interface UnreadNotificationCount {
  count: number;
}
