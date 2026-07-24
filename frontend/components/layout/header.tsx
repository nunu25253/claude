"use client";

import { usePathname } from "next/navigation";
import { Menu } from "lucide-react";
import { useAuth } from "@/lib/auth/auth-context";
import { NAV_ITEMS } from "./nav-items";
import { ThemeToggle } from "./theme-toggle";

function currentPageLabel(pathname: string): string {
  const match = NAV_ITEMS.find((item) =>
    item.href === "/" ? pathname === "/" : pathname.startsWith(item.href),
  );
  return match?.label ?? "ダッシュボード";
}

export function Header({ onMenuClick }: { onMenuClick: () => void }) {
  const pathname = usePathname();
  const { user } = useAuth();

  return (
    <header className="flex items-center justify-between border-b border-slate-200 bg-white px-4 py-3 dark:border-slate-800 dark:bg-slate-900 sm:px-6">
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={onMenuClick}
          className="rounded-md p-2 text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800 lg:hidden"
          aria-label="メニューを開く"
        >
          <Menu className="h-5 w-5" aria-hidden />
        </button>
        <h1 className="text-lg font-semibold text-slate-900 dark:text-slate-100">
          {currentPageLabel(pathname)}
        </h1>
      </div>

      <div className="flex items-center gap-3">
        <ThemeToggle />
        <div className="hidden text-right sm:block">
          <p className="text-sm font-medium text-slate-700 dark:text-slate-200">
            {user?.displayName ?? "ゲスト"}
          </p>
          <p className="text-xs text-slate-500 dark:text-slate-400">{user?.email}</p>
        </div>
        <div
          className="flex h-9 w-9 items-center justify-center rounded-full bg-brand-100 text-sm font-semibold text-brand-700 dark:bg-brand-900 dark:text-brand-200"
          aria-hidden
        >
          {(user?.displayName ?? "?").slice(0, 1).toUpperCase()}
        </div>
      </div>
    </header>
  );
}
