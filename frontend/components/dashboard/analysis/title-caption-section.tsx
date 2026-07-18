import { Card, CardHeader } from "@/components/ui/card";
import type { PostAnalysis } from "@/lib/types";

export function TitleCaptionSection({
  titleAnalysis,
  captionAnalysis,
}: {
  titleAnalysis: PostAnalysis["titleAnalysis"];
  captionAnalysis: PostAnalysis["captionAnalysis"];
}) {
  return (
    <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
      <Card>
        <CardHeader title="📝 タイトル分析" />
        <p className="text-sm text-slate-600">{titleAnalysis.summary}</p>
        <div className="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-2">
          <div>
            <p className="mb-1 text-xs font-semibold text-emerald-600">強み</p>
            <ul className="space-y-1 text-xs text-slate-600">
              {titleAnalysis.strengths.map((s, i) => (
                <li key={i}>・{s}</li>
              ))}
            </ul>
          </div>
          <div>
            <p className="mb-1 text-xs font-semibold text-red-500">弱み</p>
            <ul className="space-y-1 text-xs text-slate-600">
              {titleAnalysis.weaknesses.map((s, i) => (
                <li key={i}>・{s}</li>
              ))}
            </ul>
          </div>
        </div>
      </Card>

      <Card>
        <CardHeader title="✍️ 文章分析" />
        <p className="text-sm text-slate-600">{captionAnalysis.summary}</p>
        <div className="mt-3 flex items-center gap-4 text-sm">
          <span className="text-slate-500">トーン: {captionAnalysis.tone}</span>
          <span className="text-slate-500">
            可読性スコア: <span className="font-semibold text-slate-800">{captionAnalysis.readabilityScore}</span>/100
          </span>
        </div>
      </Card>
    </div>
  );
}
