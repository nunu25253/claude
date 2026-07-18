import Link from "next/link";

export default function NotFound() {
  return (
    <div className="flex min-h-dvh flex-col items-center justify-center gap-3 bg-slate-50 px-4 text-center">
      <p className="text-4xl">🧭</p>
      <h1 className="text-xl font-bold text-slate-900">ページが見つかりません</h1>
      <p className="text-sm text-slate-500">お探しのページは存在しないか、移動した可能性があります。</p>
      <Link href="/" className="btn-primary mt-2">
        ホームに戻る
      </Link>
    </div>
  );
}
