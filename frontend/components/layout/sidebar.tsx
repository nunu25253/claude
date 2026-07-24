"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { LogOut } from "lucide-react";
import { cn } from "@/lib/utils";
import { useAuth } from "@/lib/auth/auth-context";
import { BrandMark } from "./brand-mark";
import { NAV_GROUPS } from "./nav-items";

function isActivePath(pathname: string, href: string): boolean {
  if (href === "/") return pathname === "/";
  return pathname === href || pathname.startsWith(`${href}/`);
}

export function Sidebar({ onNavigate }: { onNavigate?: () => void }) {
  const pathname = usePathname();
  const { logout } = useAuth();

  return (
    <div className="flex h-full flex-col bg-slate-900 text-slate-200">
      <div className="flex items-center gap-2.5 px-5 py-5">
        <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-brand-600">
          <BrandMark className="h-5 w-5 text-white" />
        </div>
        <div>
          <p className="text-sm font-bold leading-tight text-white">Buzzly</p>
          <p className="text-xs text-slate-400">AI Buzz Analytics</p>
        </div>
      </div>

      <nav className="flex-1 space-y-5 overflow-y-auto px-3 pb-3" aria-label="ダッシュボードナビゲーション">
        {NAV_GROUPS.map((group, groupIndex) => (
          <div key={group.label ?? `group-${groupIndex}`} className="space-y-1">
            {group.label && (
              <p className="px-3 pb-1 text-xs font-semibold uppercase tracking-wider text-slate-500">
                {group.label}
              </p>
            )}
            {group.items.map((item) => {
              const active = isActivePath(pathname, item.href);
              const Icon = item.icon;
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  onClick={onNavigate}
                  aria-current={active ? "page" : undefined}
                  className={cn(
                    "flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition",
                    active
                      ? "bg-brand-600 text-white"
                      : "text-slate-300 hover:bg-slate-800 hover:text-white",
                  )}
                >
                  <Icon className="h-4 w-4 shrink-0" aria-hidden />
                  {item.label}
                </Link>
              );
            })}
          </div>
        ))}
      </nav>

      <div className="border-t border-slate-800 px-3 py-4">
        <button
          type="button"
          onClick={() => logout()}
          className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-slate-300 transition hover:bg-slate-800 hover:text-white"
        >
          <LogOut className="h-4 w-4" aria-hidden />
          ログアウト
        </button>
      </div>
    </div>
  );
}
