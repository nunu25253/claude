# Studio Grid — Portfolio

グリッド/雑誌(エディトリアル)風レイアウトを主題にしたポートフォリオサイトのテンプレートです。
React + Vite + TypeScript で構築しています。

## セットアップ

```bash
npm install
npm run dev
```

`npm run build` で本番ビルド、`npm run preview` でビルド後のプレビューができます。

## 構成

- `src/components/Hero.tsx` — 非対称グリッドの見出しセクション
- `src/components/ProjectGrid.tsx` / `ProjectCard.tsx` — CSS Grid (`grid-auto-flow: dense`) による雑誌風の作品一覧グリッド。サイズは `src/data/projects.ts` の `size` (`lg` / `wide` / `md` / `sm`) で調整
- `src/components/About.tsx` — 2カラムの引用+本文レイアウト
- `src/components/Contact.tsx` — ダークトーンのCTA + リンク一覧
- `src/styles/global.css` — カラー・タイポグラフィのデザイントークン

## カスタマイズ

- 名前・肩書き・本文は `Hero.tsx` / `About.tsx` / `Contact.tsx` 内の日本語テキストを直接編集してください。
- 作品データは `src/data/projects.ts` を編集(画像を使う場合は `ProjectCard.tsx` の `.thumb` にサムネイル画像を追加)。
- 配色は `src/styles/global.css` の `--color-*` 変数を変更するだけで全体に反映されます。
