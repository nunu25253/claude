import {
  Home,
  TrendingUp,
  Search,
  Brain,
  Lightbulb,
  ClipboardCheck,
  FileText,
  Trophy,
  Bookmark,
  Settings,
  CreditCard,
  type LucideIcon,
} from "lucide-react";

export interface NavItem {
  href: string;
  label: string;
  icon: LucideIcon;
}

export interface NavGroup {
  /** グループ見出し。undefinedの場合は見出し無し(先頭のホーム単体グループ用) */
  label?: string;
  items: NavItem[];
}

// サイドバーに表示するダッシュボード画面一覧(要件の9画面。AI企画/投稿評価はPhase17で追加)。
// 12画面がフラットな1リストだと関連性が分かりづらいため、機能領域ごとにグルーピングする。
export const NAV_GROUPS: NavGroup[] = [
  {
    items: [{ href: "/", label: "ホーム", icon: Home }],
  },
  {
    label: "分析",
    items: [
      { href: "/trend", label: "トレンド", icon: TrendingUp },
      { href: "/competitors", label: "競合分析", icon: Search },
      { href: "/rankings", label: "ランキング", icon: Trophy },
    ],
  },
  {
    label: "AIマーケティング",
    items: [
      { href: "/posts/analyze", label: "投稿分析", icon: Brain },
      { href: "/proposals", label: "AI企画", icon: Lightbulb },
      { href: "/evaluations", label: "投稿評価", icon: ClipboardCheck },
      { href: "/reports", label: "AIレポート", icon: FileText },
    ],
  },
  {
    label: "管理",
    items: [
      { href: "/saved", label: "保存済み分析", icon: Bookmark },
      { href: "/billing", label: "料金プラン", icon: CreditCard },
      { href: "/settings", label: "設定", icon: Settings },
    ],
  },
];

// 後方互換用: グルーピング前のフラットな一覧が必要な箇所(オンボーディングツアー等)向け
export const NAV_ITEMS: NavItem[] = NAV_GROUPS.flatMap((group) => group.items);
