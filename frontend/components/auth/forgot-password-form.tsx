"use client";

import { useState } from "react";
import Link from "next/link";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { authApi } from "@/lib/api";
import { ApiError } from "@/lib/types/common";
import { Button } from "@/components/ui/button";
import { FormField, inputClassName } from "@/components/ui/form-field";

const schema = z.object({
  email: z.string().min(1, "メールアドレスを入力してください").email("メールアドレスの形式が正しくありません"),
});

type FormValues = z.infer<typeof schema>;

export function ForgotPasswordForm() {
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [sent, setSent] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) });

  const onSubmit = async (values: FormValues) => {
    setSubmitError(null);
    try {
      await authApi.requestPasswordReset(values);
      // メールアドレスが存在するかどうかに関わらず同じメッセージを出す(アカウント列挙防止)。
      setSent(true);
    } catch (err) {
      setSubmitError(
        err instanceof ApiError ? err.message : "送信に失敗しました。時間をおいて再度お試しください。",
      );
    }
  };

  if (sent) {
    return (
      <div className="space-y-4 text-center">
        <p className="text-sm text-slate-700">
          ご入力いただいたメールアドレス宛にパスワード再設定用のメールを送信しました。届いたメールのリンクから新しいパスワードを設定してください。
        </p>
        <Link href="/login" className="text-sm font-medium text-brand-600 hover:underline">
          ログイン画面に戻る
        </Link>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
      <p className="text-sm text-slate-500">
        登録済みのメールアドレスを入力してください。パスワード再設定用のリンクをお送りします。
      </p>

      <FormField label="メールアドレス" htmlFor="email" error={errors.email?.message}>
        <input
          id="email"
          type="email"
          autoComplete="email"
          className={inputClassName(!!errors.email)}
          placeholder="you@example.com"
          {...register("email")}
        />
      </FormField>

      {submitError && (
        <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">
          {submitError}
        </p>
      )}

      <Button type="submit" isLoading={isSubmitting} className="w-full">
        再設定メールを送信
      </Button>

      <p className="text-center text-sm text-slate-500">
        <Link href="/login" className="font-medium text-brand-600 hover:underline">
          ログイン画面に戻る
        </Link>
      </p>
    </form>
  );
}
