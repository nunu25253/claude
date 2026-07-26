import { afterEach, describe, expect, it, vi } from "vitest";
import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { NotificationBell } from "./notification-bell";
import type { Notification } from "@/lib/types";

const markReadMock = vi.fn();
const markAllReadMock = vi.fn();
let unreadCountData: { count: number } | undefined;
let notificationsData: Notification[] | undefined;
let notificationsLoading = false;

vi.mock("@/lib/hooks/use-notifications", () => ({
  useUnreadNotificationCount: () => ({ data: unreadCountData }),
  useNotifications: () => ({ data: notificationsData, isLoading: notificationsLoading }),
  useMarkNotificationRead: () => ({ mutate: markReadMock }),
  useMarkAllNotificationsRead: () => ({ mutate: markAllReadMock }),
}));

function notification(overrides: Partial<Notification> = {}): Notification {
  return {
    id: "n1",
    type: "THRESHOLD_ALERT",
    title: "設定したしきい値を超えました",
    body: "現在のBuzzScore: 85.0 (しきい値: 80.0)",
    link: "/saved",
    createdAt: "2026-07-26T09:00:00Z",
    unread: true,
    ...overrides,
  };
}

describe("NotificationBell", () => {
  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
    unreadCountData = undefined;
    notificationsData = undefined;
    notificationsLoading = false;
  });

  it("shows the unread count badge when there are unread notifications", () => {
    unreadCountData = { count: 3 };
    render(<NotificationBell />);

    expect(screen.getByRole("button", { name: "通知(未読3件)" })).toBeInTheDocument();
    expect(screen.getByText("3")).toBeInTheDocument();
  });

  it("does not show a badge when there are no unread notifications", () => {
    unreadCountData = { count: 0 };
    render(<NotificationBell />);

    expect(screen.getByRole("button", { name: "通知" })).toBeInTheDocument();
  });

  it("caps the badge at 9+ for large unread counts", () => {
    unreadCountData = { count: 42 };
    render(<NotificationBell />);

    expect(screen.getByText("9+")).toBeInTheDocument();
  });

  it("shows an empty state when there are no notifications", () => {
    unreadCountData = { count: 0 };
    notificationsData = [];
    render(<NotificationBell />);

    fireEvent.click(screen.getByRole("button", { name: "通知" }));

    expect(screen.getByText("通知はまだありません")).toBeInTheDocument();
  });

  it("renders notifications and marks one as read on click", () => {
    unreadCountData = { count: 1 };
    notificationsData = [notification()];
    render(<NotificationBell />);

    fireEvent.click(screen.getByRole("button", { name: "通知(未読1件)" }));
    expect(screen.getByText("設定したしきい値を超えました")).toBeInTheDocument();

    fireEvent.click(screen.getByText("設定したしきい値を超えました"));
    expect(markReadMock).toHaveBeenCalledWith("n1");
  });

  it("calls markAllRead when 'すべて既読にする' is clicked", () => {
    unreadCountData = { count: 2 };
    notificationsData = [notification({ id: "n1" }), notification({ id: "n2" })];
    render(<NotificationBell />);

    fireEvent.click(screen.getByRole("button", { name: "通知(未読2件)" }));
    fireEvent.click(screen.getByText("すべて既読にする"));

    expect(markAllReadMock).toHaveBeenCalledTimes(1);
  });
});
