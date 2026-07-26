import { redirect } from "next/navigation";

// チーム機能は設定画面のサブセクションへ統合した(戦略監査レポート3章: 独立ナビ項目からの格下げ)。
// 既存のブックマーク/リンクを404にしないよう、このURLは設定画面へリダイレクトする。
export default function TeamPage() {
  redirect("/settings");
}
