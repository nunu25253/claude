/**
 * rechartsは重量級(gzip前で数百KB)のため各チャートはnext/dynamicで遅延読み込みする。
 * その読み込み中に表示する、レイアウトシフトを防ぐためのプレースホルダー。
 */
export function ChartSkeleton({ height = 260 }: { height?: number }) {
  return (
    <div
      className="w-full animate-pulse rounded-lg bg-slate-100"
      style={{ height }}
      aria-hidden="true"
    />
  );
}
