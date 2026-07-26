"use client";

import { useEffect, useState } from "react";
import { QueryState } from "@/components/dashboard/query-state";
import { CreateOrganizationForm } from "@/components/dashboard/team/create-organization-form";
import { MemberManagementPanel } from "@/components/dashboard/team/member-management-panel";
import { TeamSavedAnalysesPanel } from "@/components/dashboard/team/team-saved-analyses-panel";
import { useOrganizations } from "@/lib/hooks/use-organizations";
import { useAuth } from "@/lib/auth/auth-context";
import { cn } from "@/lib/utils";

/**
 * チーム機能(組織作成/メンバー管理/共有保存分析)を設定画面のサブセクションとして表示する。
 * 共同編集ワークフロー等の本格的な協業機能が実装されるまでは独立したナビ項目にせず、
 * 設定内に格下げして「今すぐ本格的に使える機能」との境界を誠実に示す(戦略監査レポート3章)。
 */
export function TeamSettingsSection() {
  const { user } = useAuth();
  const organizationsQuery = useOrganizations();
  const [selectedOrgId, setSelectedOrgId] = useState<string | null>(null);

  useEffect(() => {
    const firstOrg = organizationsQuery.data?.[0];
    if (!selectedOrgId && firstOrg) {
      setSelectedOrgId(firstOrg.id);
    }
  }, [organizationsQuery.data, selectedOrgId]);

  const selectedOrg = organizationsQuery.data?.find((org) => org.id === selectedOrgId) ?? null;

  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-sm font-semibold text-slate-900 dark:text-slate-100">チームを作成</h3>
        <p className="mt-0.5 text-sm text-slate-500">
          チームを作成すると、招待したメンバーと保存済み分析を共有できます
        </p>
        <div className="mt-3">
          <CreateOrganizationForm onCreated={(org) => setSelectedOrgId(org.id)} />
        </div>
      </div>

      <div>
        <h3 className="text-sm font-semibold text-slate-900 dark:text-slate-100">所属チーム一覧</h3>
        <div className="mt-3">
          <QueryState
            isLoading={organizationsQuery.isLoading}
            isError={organizationsQuery.isError}
            error={organizationsQuery.error}
            isEmpty={(organizationsQuery.data?.length ?? 0) === 0}
            emptyTitle="所属しているチームがありません"
            emptyDescription="上のフォームからチームを作成してください"
            onRetry={() => organizationsQuery.refetch()}
          >
            <ul className="flex flex-wrap gap-2">
              {organizationsQuery.data?.map((org) => (
                <li key={org.id}>
                  <button
                    type="button"
                    onClick={() => setSelectedOrgId(org.id)}
                    className={cn(
                      "rounded-lg border px-3 py-1.5 text-sm font-medium transition",
                      org.id === selectedOrgId
                        ? "border-brand-600 bg-brand-50 text-brand-700"
                        : "border-slate-200 text-slate-600 hover:bg-slate-50",
                    )}
                  >
                    {org.name}
                  </button>
                </li>
              ))}
            </ul>
          </QueryState>
        </div>
      </div>

      {selectedOrg && (
        <>
          <div>
            <h3 className="text-sm font-semibold text-slate-900 dark:text-slate-100">
              メンバー管理: {selectedOrg.name}
            </h3>
            <p className="mt-0.5 text-sm text-slate-500">
              {selectedOrg.myRole === "OWNER"
                ? "OWNERとしてメンバーの招待・削除ができます"
                : "メンバー一覧を閲覧できます"}
            </p>
            <div className="mt-3">
              <MemberManagementPanel
                organizationId={selectedOrg.id}
                myRole={selectedOrg.myRole}
                currentUserId={user?.id}
              />
            </div>
          </div>

          <div>
            <h3 className="text-sm font-semibold text-slate-900 dark:text-slate-100">チームの保存済み分析</h3>
            <p className="mt-0.5 text-sm text-slate-500">
              チームメンバー全員が保存した分析結果をまとめて確認できます
            </p>
            <div className="mt-3">
              <TeamSavedAnalysesPanel organizationId={selectedOrg.id} />
            </div>
          </div>
        </>
      )}
    </div>
  );
}
