"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useAuth } from "@/lib/auth/auth-context";
import { ApiError } from "@/lib/types/common";
import { Button } from "@/components/ui/button";
import { FormField, inputClassName } from "@/components/ui/form-field";
import { TurnstileWidget } from "@/components/auth/turnstile-widget";

const registerSchema = z
  .object({
    displayName: z.string().min(1, "表示名を入力してください").max(50, "50文字以内で入力してください"),
    email: z.string().min(1, "メールアドレスを入力してください").email("メールアドレスの形式が正しくありません"),
    password: z.string().min(8, "パスワードは8文字以上で入力してください"),
    passwordConfirm: z.string().min(1, "確認用パスワードを入力してください"),
  })
  .refine((data) => data.password === data.passwordConfirm, {
    message: "パスワードが一致しません",
    path: ["passwordConfirm"],
  });

type RegisterFormValues = z.infer<typeof registerSchema>;

export function RegisterForm() {
  const { register: registerUser } = useAuth();
  const router = useRouter();
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [captchaToken, setCaptchaToken] = useState<string | undefined>(undefined);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
  });

  const onSubmit = async (values: RegisterFormValues) => {
    setSubmitError(null);
    try {
      await registerUser({
        displayName: values.displayName,
        email: values.email,
        password: values.password,
        captchaToken,
      });
      router.push("/");
    } catch (err) {
      setSubmitError(
        err instanceof ApiError ? err.message : "登録に失敗しました。時間をおいて再度お試しください。",
      );
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
      <FormField label="表示名" htmlFor="displayName" error={errors.displayName?.message}>
        <input
          id="displayName"
          type="text"
          autoComplete="name"
          className={inputClassName(!!errors.displayName)}
          placeholder="山田 太郎"
          {...register("displayName")}
        />
      </FormField>

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

      <FormField label="パスワード" htmlFor="password" error={errors.password?.message} hint="8文字以上で入力してください">
        <input
          id="password"
          type="password"
          autoComplete="new-password"
          className={inputClassName(!!errors.password)}
          placeholder="••••••••"
          {...register("password")}
        />
      </FormField>

      <FormField
        label="パスワード（確認）"
        htmlFor="passwordConfirm"
        error={errors.passwordConfirm?.message}
      >
        <input
          id="passwordConfirm"
          type="password"
          autoComplete="new-password"
          className={inputClassName(!!errors.passwordConfirm)}
          placeholder="••••••••"
          {...register("passwordConfirm")}
        />
      </FormField>

      <TurnstileWidget onVerify={setCaptchaToken} onExpire={() => setCaptchaToken(undefined)} />

      {submitError && (
        <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">
          {submitError}
        </p>
      )}

      <Button type="submit" isLoading={isSubmitting} className="w-full">
        新規登録
      </Button>

      <p className="text-center text-sm text-slate-500">
        すでにアカウントをお持ちですか？{" "}
        <Link href="/login" className="font-medium text-brand-600 hover:underline">
          ログイン
        </Link>
      </p>
    </form>
  );
}
