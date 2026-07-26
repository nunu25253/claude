import { test, expect } from "@playwright/test";
import { expectNoA11yViolations } from "./a11y";

/**
 * 課金プラン(改善計画No.11: 無料枠/有料枠)の主要導線を検証する。
 * FREEプランでの上限表示→アップグレード(疑似決済)→PROプランの上限表示・次回更新日表示→解約→
 * FREEプランへの復帰、までを確認する。あわせてaxe-coreによるアクセシビリティ回帰検知も行う。
 */
test("課金プランのアップグレード・解約が壊れない", async ({ page }) => {
  const uniqueEmail = `e2e-billing-${Date.now()}@example.com`;

  await page.goto("/register");
  await page.fill("#displayName", "課金テストユーザー");
  await page.fill("#email", uniqueEmail);
  await page.fill("#password", "password123");
  await page.fill("#passwordConfirm", "password123");
  await page.click('button[type="submit"]');
  await expect(page).toHaveURL("/", { timeout: 15_000 });
  // オンボーディングツアーのモーダルは初回ユーザー情報取得後に非同期でマウントされるため、
  // 即座のisVisible()チェックでは表示前後のタイミングを取りこぼすことがある。
  // 数秒待ってでも表示されれば閉じる、出なければそのまま進める。
  // (主要CTAは/posts/analyzeへ遷移してしまうため、遷移を伴わないEscapeで閉じる)
  await page.getByRole("dialog").waitFor({ state: "visible", timeout: 3_000 }).catch(() => {});
  await page.keyboard.press("Escape").catch(() => {});

  await page.goto("/billing", { waitUntil: "networkidle" });
  await expect(page.getByText("FREEプラン")).toBeVisible({ timeout: 10_000 });
  await expect(page.getByText("1日あたりの投稿分析上限: 50回")).toBeVisible();
  await expectNoA11yViolations(page);

  await page.fill("#cardToken", "tok_e2e_test");
  await page.click('button:has-text("アップグレードする")');

  await expect(page.getByText("PROプラン")).toBeVisible({ timeout: 10_000 });
  await expect(page.getByText("1日あたりの投稿分析上限: 500回")).toBeVisible();
  await expect(page.getByText("次回更新日:")).toBeVisible();
  await expectNoA11yViolations(page);

  await page.click('button:has-text("PROプランを解約する")');
  await expect(page.getByText("FREEプラン")).toBeVisible({ timeout: 10_000 });
  await expect(page.getByText("1日あたりの投稿分析上限: 50回")).toBeVisible();
});
