import Link from "next/link";

const LEGAL_LINKS = [
  { href: "/terms", label: "利用規約" },
  { href: "/privacy", label: "プライバシーポリシー" },
  { href: "/legal", label: "特定商取引法に基づく表記" },
];

/**
 * 利用規約・プライバシーポリシー・特定商取引法ページへの導線を常時提供するフッター。
 * これらのページが存在すること自体はサービスの信頼性(特にビジネス利用検討時)に直結するため、
 * ランディングページ・認証ページ・ダッシュボードいずれの文脈でも到達可能にする
 * (シニアレビューで指摘された「法的ページへの導線が一切無い」状態への対応)。
 */
export function SiteFooter() {
  return (
    <footer className="border-t border-slate-200 py-6 text-xs text-slate-500 dark:border-slate-800 dark:text-slate-400">
      <div className="mx-auto flex max-w-4xl flex-wrap items-center justify-center gap-x-6 gap-y-2 px-4">
        {LEGAL_LINKS.map((link) => (
          <Link key={link.href} href={link.href} className="hover:text-brand-600 dark:hover:text-brand-400">
            {link.label}
          </Link>
        ))}
        <span>&copy; {new Date().getFullYear()} Buzzly</span>
      </div>
    </footer>
  );
}
