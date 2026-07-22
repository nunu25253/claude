"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { authApi } from "@/lib/api";
import { ApiError } from "@/lib/types/common";
import { Button } from "@/components/ui/button";
import { FormField, inputClassName } from "@/components/ui/form-field";

const schema = z
  .object({
    newPassword: z.string().min(8, "パスワードは8文字以上で入力してください"),
    confirmPassword: z.string().min(1, "確認のためもう一度入力してください"),
  })
  .refine((values) => values.newPassword === values.confirmPassword, {
    message: "パスワードが一致しません",
    path: ["confirmPassword"],
  });

type FormValues = z.infer<typeof schema>;

export function ResetPasswordForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const token = searchParams.get("token");
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [done, setDone] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) });

  const onSubmit = async (values: FormValues) => {
    if (!token) {
      setSubmitError("リンクが無効です。パスワード再設定を最初からやり直してください。");
      return;
    }
    setSubmitError(null);
    try {
      await authApi.confirmPasswordReset({ token, newPassword: values.newPassword });
      setDone(true);
    } catch (err) {
      setSubmitError(
        err instanceof ApiError ? err.message : "パスワードの更新に失敗しました。時間をおいて再度お試しください。",
      );
    }
  };

  if (!token) {
    return (
      <div className="space-y-4 text-center">
        <p className="text-sm text-red-600">リンクが無効です。パスワード再設定を最初からやり直してください。</p>
        <Link href="/forgot-password" className="text-sm font-medium text-brand-600 hover:underline">
          パスワード再設定をやり直す
        </Link>
      </div>
    );
  }

  if (done) {
    return (
      <div className="space-y-4 text-center">
        <p className="text-sm text-slate-700">パスワードを更新しました。新しいパスワードでログインしてください。</p>
        <Button onClick={() => router.push("/login")} className="w-full">
          ログイン画面へ
        </Button>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
      <p className="text-sm text-slate-500">新しいパスワードを入力してください。</p>

      <FormField label="新しいパスワード" htmlFor="newPassword" error={errors.newPassword?.message}>
        <input
          id="newPassword"
          type="password"
          autoComplete="new-password"
          className={inputClassName(!!errors.newPassword)}
          placeholder="••••••••"
          {...register("newPassword")}
        />
      </FormField>

      <FormField label="新しいパスワード（確認）" htmlFor="confirmPassword" error={errors.confirmPassword?.message}>
        <input
          id="confirmPassword"
          type="password"
          autoComplete="new-password"
          className={inputClassName(!!errors.confirmPassword)}
          placeholder="••••••••"
          {...register("confirmPassword")}
        />
      </FormField>

      {submitError && (
        <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">
          {submitError}
        </p>
      )}

      <Button type="submit" isLoading={isSubmitting} className="w-full">
        パスワードを更新
      </Button>
    </form>
  );
}
