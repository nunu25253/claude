"use client";

import { useEffect, useState } from "react";
import { Card } from "@/components/ui/card";

const STAGES = [
  { label: "投稿データを取得中", seconds: 3 },
  { label: "AIが投稿内容を分析中", seconds: 10 },
  { label: "BuzzScoreを算出中", seconds: 5 },
  { label: "改善案・類似投稿を生成中", seconds: 8 },
] as const;

/**
 * 投稿分析APIは同期実装(1回のリクエストが完了するまで応答が返らない)のため、
 * サーバー側から本物の進捗イベントを受け取ることはできない。数十秒の無反応が
 * 離脱要因になっていたため、経過時間に応じた擬似的な段階表示で「何が起きているか」
 * を伝え、体感の不安を減らすことを目的とする(実際の処理段階と厳密には一致しない)。
 */
export function AnalyzeProgress() {
  const [stageIndex, setStageIndex] = useState(0);
  const [elapsedSeconds, setElapsedSeconds] = useState(0);

  useEffect(() => {
    const tick = setInterval(() => setElapsedSeconds((s) => s + 1), 1000);
    return () => clearInterval(tick);
  }, []);

  useEffect(() => {
    let cumulativeSeconds = 0;
    const timers: ReturnType<typeof setTimeout>[] = [];
    STAGES.forEach((stage, index) => {
      cumulativeSeconds += stage.seconds;
      if (index === STAGES.length - 1) return; // 最終段階は完了(実レスポンス到着)まで表示し続ける
      const nextStageIndex = index + 1;
      timers.push(setTimeout(() => setStageIndex(nextStageIndex), cumulativeSeconds * 1000));
    });
    return () => timers.forEach(clearTimeout);
  }, []);

  return (
    <Card>
      <div className="flex flex-col items-center gap-5 py-6">
        <div className="flex flex-col gap-3 self-stretch">
          {STAGES.map((stage, i) => {
            const isDone = i < stageIndex;
            const isCurrent = i === stageIndex;
            return (
              <div key={stage.label} className="flex items-center gap-3">
                <span
                  className={
                    "flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-xs font-bold " +
                    (isDone
                      ? "bg-brand-600 text-white"
                      : isCurrent
                        ? "border-2 border-brand-500 text-brand-600"
                        : "border border-slate-200 text-slate-300")
                  }
                >
                  {isDone ? "✓" : i + 1}
                </span>
                <span className={isDone || isCurrent ? "text-sm text-slate-700" : "text-sm text-slate-500 dark:text-slate-400"}>
                  {stage.label}
                  {isCurrent && <span className="ml-1 animate-pulse">…</span>}
                </span>
              </div>
            );
          })}
        </div>
        <p className="text-xs text-slate-500 dark:text-slate-400">
          経過時間: {elapsedSeconds}秒(通常20〜30秒程度で完了します。長引く場合もそのままお待ちください)
        </p>
      </div>
    </Card>
  );
}
