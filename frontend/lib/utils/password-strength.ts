// よく使われる弱いパスワードの簡易denylist。zxcvbn等の大型ライブラリを追加せず、
// バンドルサイズを抑えつつ最低限の「よくある弱いパスワード」検知だけを行う。
const COMMON_PASSWORDS = new Set([
  "password",
  "password1",
  "password123",
  "12345678",
  "123456789",
  "1234567890",
  "qwerty123",
  "letmein",
  "admin1234",
  "iloveyou",
]);

export type PasswordStrengthScore = 0 | 1 | 2 | 3 | 4;

export interface PasswordStrength {
  score: PasswordStrengthScore;
  label: "とても弱い" | "弱い" | "普通" | "強い" | "とても強い";
}

const LABELS: Record<PasswordStrengthScore, PasswordStrength["label"]> = {
  0: "とても弱い",
  1: "弱い",
  2: "普通",
  3: "強い",
  4: "とても強い",
};

/**
 * 文字種の多様性・長さ・よくある弱いパスワードとの一致を見た簡易スコアリング。
 * バックエンドの検証ルール(8文字以上)自体は変更せず、あくまでユーザーへの視覚的な注意喚起。
 */
export function scorePasswordStrength(password: string): PasswordStrength {
  if (password.length === 0) {
    return { score: 0, label: LABELS[0] };
  }

  if (COMMON_PASSWORDS.has(password.toLowerCase())) {
    return { score: 0, label: LABELS[0] };
  }

  let variety = 0;
  if (/[a-z]/.test(password)) variety += 1;
  if (/[A-Z]/.test(password)) variety += 1;
  if (/[0-9]/.test(password)) variety += 1;
  if (/[^a-zA-Z0-9]/.test(password)) variety += 1;

  let score = 0;
  if (password.length >= 8) score += 1;
  if (password.length >= 12) score += 1;
  score += Math.max(0, variety - 1);

  const clamped = Math.min(4, score) as PasswordStrengthScore;
  return { score: clamped, label: LABELS[clamped] };
}
