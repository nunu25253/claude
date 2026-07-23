/**
 * アプリ共通のブランドマーク(稲妻=「バズ」を表す抽象モチーフ)。
 * app/icon.svg(ファビコン)と同じpathデータを使い、サイドバー・ランディングページ・OGP画像
 * (favicon/在アプリ/SNSシェア画像でRocket絵文字とLucide Rocketアイコンが混在し、
 * ブランドマークが4箇所で3種類バラバラだった)を統一する。
 * 色はcurrentColorに従うため、配置先の背景に応じてtext-*クラスで指定する。
 */
export function BrandMark({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg" className={className} aria-hidden>
      <path d="M17.5 6 9 18h6l-1 8 8.5-12h-6l1-8z" fill="currentColor" />
    </svg>
  );
}
