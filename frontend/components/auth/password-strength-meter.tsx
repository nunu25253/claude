import { scorePasswordStrength } from "@/lib/utils/password-strength";

const BAR_COLORS = ["bg-red-500", "bg-orange-500", "bg-yellow-500", "bg-lime-500", "bg-green-500"];

/**
 * バックエンドの検証ルール(8文字以上)自体は変更せず、あくまで視覚的な注意喚起として
 * パスワード強度を表示する(シニアレビュー: パスワードポリシーが最小限、への対応)。
 */
export function PasswordStrengthMeter({ password }: { password: string }) {
  if (password.length === 0) return null;

  const { score, label } = scorePasswordStrength(password);

  return (
    <div className="mt-1.5" aria-live="polite">
      <div className="flex gap-1">
        {[0, 1, 2, 3].map((i) => (
          <div
            key={i}
            className={`h-1 flex-1 rounded-full ${i <= score - 1 ? BAR_COLORS[score] : "bg-slate-200 dark:bg-slate-700"}`}
          />
        ))}
      </div>
      <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">強度: {label}</p>
    </div>
  );
}
