"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { FormField, inputClassName } from "@/components/ui/form-field";
import { useProfileSettings, useUpdateProfileSettings } from "@/lib/hooks/use-settings";
import { useAuth } from "@/lib/auth/auth-context";
import { QueryState } from "@/components/dashboard/query-state";

const profileSchema = z.object({
  displayName: z.string().min(1, "表示名を入力してください").max(50),
  email: z.string().min(1, "メールアドレスを入力してください").email("メールアドレスの形式が正しくありません"),
});

type ProfileFormValues = z.infer<typeof profileSchema>;

export function ProfileSettingsForm() {
  const { user } = useAuth();
  const profileQuery = useProfileSettings();
  const updateMutation = useUpdateProfileSettings();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<ProfileFormValues>({
    resolver: zodResolver(profileSchema),
    // API未接続時でもログイン中ユーザー情報を初期値として使えるようにフォールバックする
    defaultValues: {
      displayName: user?.displayName ?? "",
      email: user?.email ?? "",
    },
  });

  useEffect(() => {
    if (profileQuery.data) {
      reset(profileQuery.data);
    }
  }, [profileQuery.data, reset]);

  return (
    <QueryState
      isLoading={profileQuery.isLoading}
      isError={profileQuery.isError && !user}
      error={profileQuery.error}
      onRetry={() => profileQuery.refetch()}
    >
      <form
        onSubmit={handleSubmit((values) => updateMutation.mutate(values))}
        noValidate
        className="space-y-4"
      >
        <FormField label="表示名" htmlFor="displayName" error={errors.displayName?.message}>
          <input
            id="displayName"
            className={inputClassName(!!errors.displayName)}
            {...register("displayName")}
          />
        </FormField>
        <FormField label="メールアドレス" htmlFor="email" error={errors.email?.message}>
          <input
            id="email"
            type="email"
            className={inputClassName(!!errors.email)}
            {...register("email")}
          />
        </FormField>

        <div className="flex items-center gap-3">
          <Button type="submit" isLoading={updateMutation.isPending} disabled={!isDirty}>
            保存する
          </Button>
          {updateMutation.isSuccess && (
            <span className="text-sm text-emerald-600">保存しました</span>
          )}
          {updateMutation.isError && (
            <span className="text-sm text-red-500">保存に失敗しました</span>
          )}
        </div>
      </form>
    </QueryState>
  );
}
