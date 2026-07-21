import { Card, CardHeader } from "@/components/ui/card";

interface InsightTextCardProps {
  title: string;
  icon: string;
  text?: string;
  emptyLabel?: string;
}

/** AIが生成した1項目分の分析結果(自然文)を表示する共通カード。 */
export function InsightListCard({ title, icon, text, emptyLabel = "分析結果がありません" }: InsightTextCardProps) {
  return (
    <Card>
      <CardHeader title={`${icon} ${title}`} />
      <p className="text-sm text-slate-700">{text || emptyLabel}</p>
    </Card>
  );
}
