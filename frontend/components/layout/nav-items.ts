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
  Users,
  type LucideIcon,
} from "lucide-react";

export interface NavItem {
  href: string;
  label: string;
  icon: LucideIcon;
}

// サイドバーに表示するダッシュボード画面一覧(要件の9画面。AI企画/投稿評価はPhase17で追加)
export const NAV_ITEMS: NavItem[] = [
  { href: "/", label: "ホーム", icon: Home },
  { href: "/trend", label: "トレンド", icon: TrendingUp },
  { href: "/competitors", label: "競合分析", icon: Search },
  { href: "/posts/analyze", label: "投稿分析", icon: Brain },
  { href: "/proposals", label: "AI企画", icon: Lightbulb },
  { href: "/evaluations", label: "投稿評価", icon: ClipboardCheck },
  { href: "/reports", label: "AIレポート", icon: FileText },
  { href: "/rankings", label: "ランキング", icon: Trophy },
  { href: "/saved", label: "保存済み分析", icon: Bookmark },
  { href: "/team", label: "チーム", icon: Users },
  { href: "/settings", label: "設定", icon: Settings },
];
