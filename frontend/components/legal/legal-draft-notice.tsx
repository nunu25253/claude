/**
 * このページの内容は一般的なSaaSの標準的な条項構成を元にしたドラフトであり、
 * 弁護士等の専門家によるレビューを経ていない。実際のサービス公開前に必ず専門家の
 * レビューを受け、事業者情報(会社名・所在地・連絡先等)を正しい値に置き換えること。
 * シニアレビューで指摘された「法的ページが一切存在しない」状態からの第一歩として、
 * まずドラフトを用意し、公開ブロッカーであることが一目でわかるよう明示する。
 */
export function LegalDraftNotice() {
  return (
    <div
      role="note"
      className="rounded-lg border border-amber-300 bg-amber-50 px-4 py-3 text-xs text-amber-800 dark:border-amber-800 dark:bg-amber-950 dark:text-amber-300"
    >
      <p className="font-semibold">このページはドラフトです</p>
      <p className="mt-1">
        本文は一般的なSaaSの標準条項を元にした草案であり、専門家(弁護士等)によるレビューを経ていません。実際の公開前に、事業者情報を含め必ず専門家の確認を受けてください。
      </p>
    </div>
  );
}
