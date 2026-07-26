"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { Bell } from "lucide-react";
import {
  useMarkAllNotificationsRead,
  useMarkNotificationRead,
  useNotifications,
  useUnreadNotificationCount,
} from "@/lib/hooks/use-notifications";
import { cn, formatDateTime } from "@/lib/utils";

/**
 * アプリ内通知センター(ヘッダーのベルアイコン)。しきい値アラート・週次ダイジェストは
 * メール通知のみに依存すると開封率が低いため、アプリ内でも同じ内容を確認できるようにする
 * (シニアレビュー: 「通知チャネルがメールのみ」への対応)。
 */
export function NotificationBell() {
  const [open, setOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  const unreadCountQuery = useUnreadNotificationCount();
  const notificationsQuery = useNotifications(open);
  const markReadMutation = useMarkNotificationRead();
  const markAllReadMutation = useMarkAllNotificationsRead();

  useEffect(() => {
    if (!open) return;
    function handleClickOutside(event: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    }
    function handleEscape(event: KeyboardEvent) {
      if (event.key === "Escape") setOpen(false);
    }
    document.addEventListener("mousedown", handleClickOutside);
    document.addEventListener("keydown", handleEscape);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      document.removeEventListener("keydown", handleEscape);
    };
  }, [open]);

  const unreadCount = unreadCountQuery.data?.count ?? 0;

  return (
    <div className="relative" ref={containerRef}>
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className="relative rounded-md p-2 text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800"
        aria-label={unreadCount > 0 ? `通知(未読${unreadCount}件)` : "通知"}
        aria-expanded={open}
      >
        <Bell className="h-5 w-5" aria-hidden />
        {unreadCount > 0 && (
          <span className="absolute -right-0.5 -top-0.5 flex h-4 min-w-[16px] items-center justify-center rounded-full bg-red-500 px-1 text-[10px] font-bold leading-none text-white">
            {unreadCount > 9 ? "9+" : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 z-20 mt-2 w-80 max-w-[90vw] rounded-xl border border-slate-200 bg-white shadow-lg dark:border-slate-800 dark:bg-slate-900">
          <div className="flex items-center justify-between border-b border-slate-100 px-4 py-2.5 dark:border-slate-800">
            <p className="text-sm font-semibold text-slate-900 dark:text-slate-100">通知</p>
            {unreadCount > 0 && (
              <button
                type="button"
                onClick={() => markAllReadMutation.mutate()}
                className="text-xs font-medium text-brand-600 hover:underline dark:text-brand-400"
              >
                すべて既読にする
              </button>
            )}
          </div>

          <div className="max-h-80 overflow-y-auto">
            {notificationsQuery.isLoading && (
              <p className="px-4 py-6 text-center text-sm text-slate-500">読み込み中…</p>
            )}
            {notificationsQuery.data?.length === 0 && (
              <p className="px-4 py-6 text-center text-sm text-slate-500">通知はまだありません</p>
            )}
            {notificationsQuery.data?.map((notification) => {
              const content = (
                <div
                  className={cn(
                    "border-b border-slate-50 px-4 py-3 last:border-0 dark:border-slate-800/60",
                    notification.unread && "bg-brand-50/60 dark:bg-brand-950/30",
                  )}
                >
                  <div className="flex items-start gap-2">
                    {notification.unread && (
                      <span className="mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-brand-600" aria-hidden />
                    )}
                    <div className="min-w-0 flex-1">
                      <p className="text-sm font-medium text-slate-900 dark:text-slate-100">{notification.title}</p>
                      <p className="mt-0.5 whitespace-pre-line text-xs text-slate-500 dark:text-slate-400">
                        {notification.body}
                      </p>
                      <p className="mt-1 text-[11px] text-slate-400">{formatDateTime(notification.createdAt)}</p>
                    </div>
                  </div>
                </div>
              );

              return (
                <Link
                  key={notification.id}
                  href={notification.link ?? "#"}
                  onClick={() => {
                    if (notification.unread) markReadMutation.mutate(notification.id);
                    setOpen(false);
                  }}
                  className="block hover:bg-slate-50 dark:hover:bg-slate-800/60"
                >
                  {content}
                </Link>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}
