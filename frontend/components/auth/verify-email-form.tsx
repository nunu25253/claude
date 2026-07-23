"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { authApi } from "@/lib/api";
import { useAuth } from "@/lib/auth/auth-context";
import { ApiError } from "@/lib/types/common";
import { Button } from "@/components/ui/button";
import { FormField, inputClassName } from "@/components/ui/form-field";

const resendSchema = z.object({
  email: z.string().min(1, "メールアドレスを入力してください").email("メールアドレスの形式が正しくありません"),
});

type ResendFormValues = z.infer<typeof resendSchema>;

type VerificationState = "verifying" | "success" | "error";

function ResendVerificationForm() {
  const [sent, setSent] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ResendFormValues>({ resolver: zodResolver(resendSchema) });

  const onSubmit = async (values: ResendFormValues) => {
    // メールアドレスが存在するか/確認済みかに関わらず同じ結果を返す(アカウント列挙防止)。
    await authApi.resendEmailVerification(values);
    setSent(true);
  };

  if (sent) {
    return (
      <p className="text-sm text-slate-700">
        入力いただいたメールアドレス宛に確認メールを送信しました。届いたメールのリンクからご確認ください。
      </p>
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
      <p className="text-sm text-slate-500">確認メールを再送します。登録したメールアドレスを入力してください。</p>

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

      <Button type="submit" isLoading={isSubmitting} className="w-full">
        確認メールを再送する
      </Button>
    </form>
  );
}

export function VerifyEmailForm() {
  const searchParams = useSearchParams();
  const token = searchParams.get("token");
  const { markEmailVerified } = useAuth();
  const [state, setState] = useState<VerificationState>(token ? "verifying" : "error");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (!token) return;
    let cancelled = false;
    authApi
      .confirmEmailVerification({ token })
      .then(() => {
        if (cancelled) return;
        markEmailVerified();
        setState("success");
      })
      .catch((err) => {
        if (cancelled) return;
        setErrorMessage(
          err instanceof ApiError ? err.message : "確認に失敗しました。リンクの有効期限が切れている可能性があります。",
        );
        setState("error");
      });
    return () => {
      cancelled = true;
    };
    // token/markEmailVerified は初回マウント時の値で確定させ、確認処理は一度だけ実行する
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (state === "verifying") {
    return <p className="text-center text-sm text-slate-500">メールアドレスを確認しています…</p>;
  }

  if (state === "success") {
    return (
      <div className="space-y-4 text-center">
        <p className="text-sm text-slate-700">メールアドレスの確認が完了しました。</p>
        <Link href="/" className="btn-primary block w-full text-center">
          ダッシュボードへ
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-center text-sm text-red-600">
        {errorMessage ?? "リンクが無効です。確認メールを再送してください。"}
      </p>
      <ResendVerificationForm />
    </div>
  );
}
