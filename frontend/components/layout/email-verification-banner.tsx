"use client";

import { useState } from "react";
import { authApi } from "@/lib/api";
import { useAuth } from "@/lib/auth/auth-context";
import { ApiError } from "@/lib/types/common";

export function EmailVerificationBanner() {
  const { user } = useAuth();
  const [sent, setSent] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!user || user.emailVerified !== false) {
    return null;
  }

  const handleResend = async () => {
    setIsSending(true);
    setError(null);
    try {
      await authApi.resendEmailVerification({ email: user.email });
      setSent(true);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "送信に失敗しました。時間をおいて再度お試しください。");
    } finally {
      setIsSending(false);
    }
  };

  return (
    <div className="mb-4 flex flex-wrap items-center justify-between gap-3 rounded-lg bg-amber-50 px-4 py-3 text-sm text-amber-800 dark:bg-amber-950 dark:text-amber-200">
      <p>
        メールアドレスがまだ確認されていません。一部の機能(投稿分析など)をご利用いただくには、確認メールのリンクからご確認ください。
      </p>
      {sent ? (
        <span className="shrink-0 font-medium">確認メールを再送しました</span>
      ) : (
        <button
          type="button"
          onClick={handleResend}
          disabled={isSending}
          className="shrink-0 font-medium underline hover:no-underline disabled:opacity-50"
        >
          確認メールを再送する
        </button>
      )}
      {error && <p className="w-full text-red-600">{error}</p>}
    </div>
  );
}
