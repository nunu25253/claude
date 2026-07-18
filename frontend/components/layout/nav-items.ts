export interface NavItem {
  href: string;
  label: string;
  icon: string;
}

// サイドバーに表示するダッシュボード画面一覧（要件の9画面。AI企画/投稿評価はPhase17で追加）
export const NAV_ITEMS: NavItem[] = [
  { href: "/", label: "ホーム", icon: "🏠" },
  { href: "/trend", label: "トレンド", icon: "📈" },
  { href: "/competitors", label: "競合分析", icon: "🔍" },
  { href: "/posts/analyze", label: "投稿分析", icon: "🧠" },
  { href: "/proposals", label: "AI企画", icon: "💡" },
  { href: "/evaluations", label: "投稿評価", icon: "📝" },
  { href: "/reports", label: "AIレポート", icon: "📄" },
  { href: "/rankings", label: "ランキング", icon: "🏆" },
  { href: "/saved", label: "保存済み分析", icon: "🔖" },
  { href: "/settings", label: "設定", icon: "⚙️" },
];
