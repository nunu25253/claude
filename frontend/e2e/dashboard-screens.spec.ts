import { test, expect } from "@playwright/test";
import { expectNoA11yViolations } from "./a11y";

/**
 * これまでE2E未整備だった閲覧系ダッシュボード画面(トレンド・ランキング・保存済み分析・設定)の
 * スモークテスト。シニアレビューで「E2Eが主要12画面中3画面しかカバーしていない」と指摘された
 * ことへの対応の第一弾として、クラッシュせずに描画されること・アクセシビリティ回帰が無いことを
 * 機械的に検知できるようにする(単体テストでは検知できない画面遷移・実API連携込みの不具合が対象)。
 */
test("トレンド・ランキング・保存済み分析・設定・案件マッチ度画面がクラッシュせず描画される", async ({ page }) => {
  const uniqueEmail = `e2e-screens-${Date.now()}@example.com`;

  await page.goto("/register");
  await page.fill("#displayName", "画面スモークテストユーザー");
  await page.fill("#email", uniqueEmail);
  await page.fill("#password", "password123");
  await page.fill("#passwordConfirm", "password123");
  await page.getByRole("checkbox").check();
  await page.click('button[type="submit"]');
  await expect(page).toHaveURL("/", { timeout: 15_000 });

  await page.getByRole("dialog").waitFor({ state: "visible", timeout: 3_000 }).catch(() => {});
  await page.keyboard.press("Escape").catch(() => {});

  const screens: Array<{ path: string; expectedText: string | RegExp }> = [
    { path: "/trend", expectedText: "トレンドハッシュタグ" },
    { path: "/rankings", expectedText: /トレンド|急上昇|総合/ },
    { path: "/saved", expectedText: /保存済み|まだ保存された分析がありません/ },
    { path: "/settings", expectedText: "プロフィール" },
    { path: "/matching", expectedText: "案件マッチ度チェック" },
  ];

  for (const screen of screens) {
    await page.goto(screen.path, { waitUntil: "networkidle" });
    const body = await page.locator("body").innerText();
    expect(body, `${screen.path} でクライアント側の例外が発生していないこと`).not.toContain("client-side exception");
    await expect(page.getByText(screen.expectedText).first()).toBeVisible({ timeout: 10_000 });
    await expectNoA11yViolations(page);
  }
});
