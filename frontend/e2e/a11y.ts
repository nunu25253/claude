import { expect, type Page } from "@playwright/test";
import AxeBuilder from "@axe-core/playwright";

/**
 * 現在表示中のページに対してaxe-core(WCAG 2.1 A/AA)によるアクセシビリティスキャンを実行し、
 * 違反が1件でもあればテストを失敗させる。手動で是正したaria/alt属性等が将来のリファクタで
 * 崩れた場合に機械的に検知するための回帰ガード。
 */
export async function expectNoA11yViolations(page: Page): Promise<void> {
  const results = await new AxeBuilder({ page }).withTags(["wcag2a", "wcag2aa"]).analyze();
  const summary = results.violations.map(
    (v) => `[${v.impact}] ${v.id}: ${v.help} (${v.nodes.length}箇所) ${v.helpUrl}`,
  );
  expect(summary, `アクセシビリティ違反が検出されました:\n${summary.join("\n")}`).toEqual([]);
}
