import { BrandMark } from "@/components/layout/brand-mark";

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-dvh items-center justify-center bg-gradient-to-br from-brand-50 via-white to-slate-50 px-4 py-10 dark:from-slate-950 dark:via-slate-950 dark:to-slate-900">
      <div className="w-full max-w-md">
        <div className="mb-6 flex flex-col items-center gap-2 text-center">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-brand-600 shadow-card">
            <BrandMark className="h-7 w-7 text-white" />
          </div>
          <h1 className="text-xl font-bold text-slate-900 dark:text-slate-100">Buzzly</h1>
          <p className="text-sm text-slate-500">
            Instagram / TikTok / X の公開投稿からAIがバズの理由を解き明かします
          </p>
        </div>
        <div className="card">{children}</div>
      </div>
    </div>
  );
}
