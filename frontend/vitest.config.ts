import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";
import path from "node:path";

/**
 * lib/配下の純粋ロジック・単体テスト用設定。
 * Playwright(e2e/*.spec.ts)とファイルパターンが衝突しないよう、対象は
 * *.test.ts(x) かつ e2e/ 配下を明示的に除外する。
 */
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "."),
    },
  },
  test: {
    environment: "jsdom",
    include: ["**/*.test.{ts,tsx}"],
    exclude: ["e2e/**", "node_modules/**", ".next/**"],
    globals: true,
  },
});
