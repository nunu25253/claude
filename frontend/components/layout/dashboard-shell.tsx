"use client";

import { useState } from "react";
import { Sidebar } from "./sidebar";
import { Header } from "./header";
import { EmailVerificationBanner } from "./email-verification-banner";
import { OnboardingTour } from "@/components/dashboard/onboarding-tour";

export function DashboardShell({ children }: { children: React.ReactNode }) {
  const [isMobileNavOpen, setIsMobileNavOpen] = useState(false);

  return (
    <div className="flex h-dvh overflow-hidden bg-slate-50 dark:bg-slate-950">
      {/* デスクトップ用の常設サイドバー */}
      <aside className="hidden w-64 shrink-0 lg:block">
        <Sidebar />
      </aside>

      {/* モバイル用のドロワーサイドバー */}
      {isMobileNavOpen && (
        <div className="fixed inset-0 z-40 flex lg:hidden">
          <div
            className="fixed inset-0 bg-black/40"
            onClick={() => setIsMobileNavOpen(false)}
            aria-hidden
          />
          <div className="relative z-50 w-64">
            <Sidebar onNavigate={() => setIsMobileNavOpen(false)} />
          </div>
        </div>
      )}

      <div className="flex min-w-0 flex-1 flex-col">
        <Header onMenuClick={() => setIsMobileNavOpen(true)} />
        <main className="flex-1 overflow-y-auto px-4 py-6 sm:px-6 lg:px-8">
          <EmailVerificationBanner />
          {children}
        </main>
      </div>

      <OnboardingTour />
    </div>
  );
}
