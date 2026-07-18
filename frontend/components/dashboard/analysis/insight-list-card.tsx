import { Card, CardHeader } from "@/components/ui/card";

interface InsightListCardProps {
  title: string;
  icon: string;
  items: string[];
  emptyLabel?: string;
}

/** 「伸びた理由」「ターゲット層」「フック」「CTA」「改善案」など文字列配列を箇条書き表示する共通カード */
export function InsightListCard({ title, icon, items, emptyLabel = "分析結果がありません" }: InsightListCardProps) {
  return (
    <Card>
      <CardHeader title={`${icon} ${title}`} />
      {items.length === 0 ? (
        <p className="text-sm text-slate-400">{emptyLabel}</p>
      ) : (
        <ul className="space-y-2 text-sm text-slate-700">
          {items.map((item, idx) => (
            <li key={idx} className="flex gap-2">
              <span className="mt-0.5 text-brand-500">・</span>
              <span>{item}</span>
            </li>
          ))}
        </ul>
      )}
    </Card>
  );
}
