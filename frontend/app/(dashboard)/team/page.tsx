"use client";

import { useEffect, useState } from "react";
import { Card, CardHeader } from "@/components/ui/card";
import { QueryState } from "@/components/dashboard/query-state";
import { CreateOrganizationForm } from "@/components/dashboard/team/create-organization-form";
import { MemberManagementPanel } from "@/components/dashboard/team/member-management-panel";
import { TeamSavedAnalysesPanel } from "@/components/dashboard/team/team-saved-analyses-panel";
import { useOrganizations } from "@/lib/hooks/use-organizations";
import { useAuth } from "@/lib/auth/auth-context";
import { cn } from "@/lib/utils";

export default function TeamPage() {
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
      <Card>
        <CardHeader
          title="チームを作成"
          description="チームを作成すると、招待したメンバーと保存済み分析を共有できます"
        />
        <CreateOrganizationForm onCreated={(org) => setSelectedOrgId(org.id)} />
      </Card>

      <Card>
        <CardHeader title="所属チーム一覧" />
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
      </Card>

      {selectedOrg && (
        <>
          <Card>
            <CardHeader
              title={`メンバー管理: ${selectedOrg.name}`}
              description={
                selectedOrg.myRole === "OWNER"
                  ? "OWNERとしてメンバーの招待・削除ができます"
                  : "メンバー一覧を閲覧できます"
              }
            />
            <MemberManagementPanel
              organizationId={selectedOrg.id}
              myRole={selectedOrg.myRole}
              currentUserId={user?.id}
            />
          </Card>

          <Card>
            <CardHeader
              title="チームの保存済み分析"
              description="チームメンバー全員が保存した分析結果をまとめて確認できます"
            />
            <TeamSavedAnalysesPanel organizationId={selectedOrg.id} />
          </Card>
        </>
      )}
    </div>
  );
}
