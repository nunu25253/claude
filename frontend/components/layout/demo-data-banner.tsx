"use client";

import { useDataMode } from "@/lib/hooks/use-system";

/**
 * SNS公式APIキーが1つも設定されていない環境では、投稿データがすべて疑似(デモ)データに
 * フォールバックする。分析結果が実在の投稿かどうかユーザーが区別できないと信頼性を損なうため、
 * 明示的にバナーで知らせる(シニアレビュアーによるレビューで指摘された問題への対応)。
 */
export function DemoDataBanner() {
  const dataModeQuery = useDataMode();
  const dataMode = dataModeQuery.data;

  if (!dataMode || dataMode.anyPlatformLive) {
    return null;
  }

  return (
    <div className="mb-4 rounded-lg bg-sky-50 px-4 py-3 text-sm text-sky-800 dark:bg-sky-950 dark:text-sky-200">
      現在、SNS公式APIキーが未設定のためデモデータで動作しています。表示される投稿・エンゲージメント数値は実際のSNS投稿ではありません。実データを利用するには管理者にお問い合わせください。
    </div>
  );
}
