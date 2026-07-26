import type { Metadata } from "next";
import { LegalPageShell } from "@/components/legal/legal-page-shell";
import { LegalDraftNotice } from "@/components/legal/legal-draft-notice";

export const metadata: Metadata = { title: "特定商取引法に基づく表記" };

const ROWS: Array<{ label: string; value: string }> = [
  { label: "販売事業者", value: "[事業者名を記載]" },
  { label: "運営責任者", value: "[運営責任者名を記載]" },
  { label: "所在地", value: "[所在地を記載](請求があった場合には遅滞なく開示します)" },
  { label: "電話番号", value: "[電話番号を記載](請求があった場合には遅滞なく開示します)" },
  { label: "メールアドレス", value: "[問い合わせ用メールアドレスを記載]" },
  { label: "販売価格", value: "料金プランページに記載の金額(税込)" },
  { label: "商品代金以外の必要料金", value: "インターネット接続にかかる通信費等はお客様のご負担となります" },
  { label: "お支払い方法", value: "クレジットカード決済(決済代行事業者経由)" },
  { label: "お支払い時期", value: "初回はお申し込み時に課金、以降は毎月同日に自動更新課金" },
  { label: "サービス提供時期", value: "決済完了後、直ちにご利用いただけます" },
  {
    label: "返品・キャンセルについて",
    value: "サービスの性質上、購入後のキャンセル・返金には応じかねます。次回更新日より前に解約手続きを行うことで、次回以降の課金を停止できます",
  },
  { label: "動作環境", value: "最新版のGoogle Chrome / Safari / Microsoft Edgeを推奨します" },
];

export default function LegalNoticePage() {
  return (
    <LegalPageShell title="特定商取引法に基づく表記" updatedAt="2026年7月26日">
      <LegalDraftNotice />

      <p>特定商取引法第11条に基づき、以下のとおり表示します。</p>

      <div className="mt-6 overflow-hidden rounded-lg border border-slate-200 dark:border-slate-800">
        <table className="w-full border-collapse text-sm">
          <tbody>
            {ROWS.map((row) => (
              <tr key={row.label} className="border-b border-slate-200 last:border-0 dark:border-slate-800">
                <th
                  scope="row"
                  className="w-40 shrink-0 bg-slate-50 px-4 py-3 text-left align-top text-xs font-semibold text-slate-500 dark:bg-slate-900 dark:text-slate-400"
                >
                  {row.label}
                </th>
                <td className="px-4 py-3 align-top text-slate-700 dark:text-slate-300">{row.value}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </LegalPageShell>
  );
}
