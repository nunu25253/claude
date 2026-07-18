import { Card, CardHeader } from "@/components/ui/card";
import type { HashtagAnalysisItem } from "@/lib/types";
import { formatCompactNumber, formatPercent } from "@/lib/utils";

export function HashtagAnalysisSection({ items }: { items: HashtagAnalysisItem[] }) {
  return (
    <Card>
      <CardHeader title="🏷️ ハッシュタグ分析" />
      {items.length === 0 ? (
        <p className="text-sm text-slate-400">分析結果がありません</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-slate-100 text-xs text-slate-400">
                <th className="py-2 font-medium">タグ</th>
                <th className="py-2 font-medium">投稿数</th>
                <th className="py-2 font-medium">平均エンゲージメント率</th>
                <th className="py-2 font-medium">推奨</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.tag} className="border-b border-slate-50 last:border-0">
                  <td className="py-2 font-medium text-slate-700">#{item.tag}</td>
                  <td className="py-2 text-slate-500">{formatCompactNumber(item.postCount)}</td>
                  <td className="py-2 text-slate-500">{formatPercent(item.avgEngagementRate)}</td>
                  <td className="py-2">
                    {item.isRecommended ? (
                      <span className="badge bg-emerald-50 text-emerald-700">推奨</span>
                    ) : (
                      <span className="badge bg-slate-100 text-slate-500">-</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Card>
  );
}
