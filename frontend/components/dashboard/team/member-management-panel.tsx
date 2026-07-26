"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { FormField, inputClassName } from "@/components/ui/form-field";
import { QueryState } from "@/components/dashboard/query-state";
import { ApiError } from "@/lib/types/common";
import {
  useInviteMember,
  useOrganizationMembers,
  useRemoveMember,
} from "@/lib/hooks/use-organizations";
import { formatDateTime } from "@/lib/utils";
import type { OrganizationRole } from "@/lib/types";

const inviteSchema = z.object({
  email: z.string().min(1, "メールアドレスを入力してください").email("メールアドレスの形式が正しくありません"),
});

type InviteFormValues = z.infer<typeof inviteSchema>;

export function MemberManagementPanel({
  organizationId,
  myRole,
  currentUserId,
}: {
  organizationId: string;
  myRole: OrganizationRole;
  currentUserId: string | undefined;
}) {
  const membersQuery = useOrganizationMembers(organizationId);
  const inviteMutation = useInviteMember(organizationId);
  const removeMutation = useRemoveMember(organizationId);
  const [inviteError, setInviteError] = useState<string | null>(null);
  const [pendingRemoveUserId, setPendingRemoveUserId] = useState<string | null>(null);
  const isOwner = myRole === "OWNER";

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<InviteFormValues>({ resolver: zodResolver(inviteSchema) });

  const onInvite = async (values: InviteFormValues) => {
    setInviteError(null);
    try {
      await inviteMutation.mutateAsync(values);
      reset();
    } catch (err) {
      setInviteError(
        err instanceof ApiError ? err.message : "招待に失敗しました。時間をおいて再度お試しください。",
      );
    }
  };

  const handleRemove = async (userId: string) => {
    setPendingRemoveUserId(userId);
    try {
      await removeMutation.mutateAsync(userId);
    } finally {
      setPendingRemoveUserId(null);
    }
  };

  return (
    <div className="space-y-4">
      {isOwner && (
        <form onSubmit={handleSubmit(onInvite)} noValidate className="flex items-start gap-3">
          <div className="flex-1">
            <FormField label="メンバーを招待" htmlFor="inviteEmail" error={errors.email?.message}>
              <input
                id="inviteEmail"
                type="email"
                placeholder="招待する登録済みユーザーのメールアドレス"
                className={inputClassName(!!errors.email)}
                {...register("email")}
              />
            </FormField>
          </div>
          <div className="pt-6">
            <Button type="submit" isLoading={inviteMutation.isPending}>
              招待する
            </Button>
          </div>
        </form>
      )}
      {inviteError && (
        <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">
          {inviteError}
        </p>
      )}

      <QueryState
        isLoading={membersQuery.isLoading}
        isError={membersQuery.isError}
        error={membersQuery.error}
        isEmpty={(membersQuery.data?.length ?? 0) === 0}
        emptyTitle="メンバーがいません"
        onRetry={() => membersQuery.refetch()}
      >
        <ul className="divide-y divide-slate-100">
          {membersQuery.data?.map((member) => (
            <li key={member.userId} className="flex items-center justify-between py-3">
              <div>
                <p className="text-sm font-medium text-slate-700">
                  {member.displayName}
                  <span className="ml-2 rounded bg-slate-100 px-1.5 py-0.5 text-xs font-normal text-slate-600">
                    {member.role === "OWNER" ? "OWNER" : "MEMBER"}
                  </span>
                </p>
                <p className="text-xs text-slate-500 dark:text-slate-400">
                  {member.email} ・ 参加日 {formatDateTime(member.joinedAt)}
                </p>
              </div>
              {isOwner && member.userId !== currentUserId && (
                <Button
                  variant="danger"
                  onClick={() => handleRemove(member.userId)}
                  isLoading={pendingRemoveUserId === member.userId}
                >
                  削除
                </Button>
              )}
            </li>
          ))}
        </ul>
      </QueryState>
    </div>
  );
}
