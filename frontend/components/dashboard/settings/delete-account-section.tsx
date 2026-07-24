"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { FormField, inputClassName } from "@/components/ui/form-field";
import { useAuth } from "@/lib/auth/auth-context";
import { ApiError } from "@/lib/types/common";

export function DeleteAccountSection() {
  const { deleteAccount } = useAuth();
  const [isConfirming, setIsConfirming] = useState(false);
  const [currentPassword, setCurrentPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const resetToInitial = () => {
    setIsConfirming(false);
    setCurrentPassword("");
    setError(null);
  };

  const handleDelete = async () => {
    if (!currentPassword) {
      setError("現在のパスワードを入力してください");
      return;
    }
    setError(null);
    setIsSubmitting(true);
    try {
      await deleteAccount(currentPassword);
    } catch (err) {
      setError(
        err instanceof ApiError
          ? err.message
          : "アカウントの削除に失敗しました。時間をおいて再度お試しください。",
      );
      setIsSubmitting(false);
    }
  };

  if (!isConfirming) {
    return (
      <div className="space-y-3">
        <p className="text-sm text-slate-500 dark:text-slate-400">
          アカウントを削除すると、保存済み分析・チーム所属・課金情報など関連するすべてのデータが
          完全に削除されます。この操作は取り消せません。
        </p>
        <Button variant="danger" onClick={() => setIsConfirming(true)}>
          アカウントを削除する
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-4 rounded-lg border border-red-200 bg-red-50 p-4 dark:border-red-900 dark:bg-red-950">
      <p className="text-sm font-medium text-red-700 dark:text-red-400">
        本当にアカウントを削除しますか?この操作は取り消せません。確認のため現在のパスワードを入力してください。
      </p>
      <FormField label="現在のパスワード" htmlFor="delete-account-password" error={error ?? undefined}>
        <input
          id="delete-account-password"
          type="password"
          autoComplete="current-password"
          className={inputClassName(!!error)}
          value={currentPassword}
          onChange={(e) => setCurrentPassword(e.target.value)}
        />
      </FormField>
      <div className="flex items-center gap-3">
        <Button variant="danger" onClick={handleDelete} isLoading={isSubmitting}>
          完全に削除する
        </Button>
        <Button variant="secondary" onClick={resetToInitial} disabled={isSubmitting}>
          キャンセル
        </Button>
      </div>
    </div>
  );
}
