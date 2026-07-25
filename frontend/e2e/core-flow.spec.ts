import { test, expect } from "@playwright/test";
import { fetchVerificationLink } from "./mail-catcher";
import { expectNoA11yViolations } from "./a11y";

/**
 * 主要導線(新規登録→メール確認→投稿分析→保存→CSVエクスポート)が壊れていないことを確認するE2Eテスト。
 * 本セッション中、この導線だけで403(JWT失効)・400(フィールド名不一致)・
 * 500(UNIQUE制約違反)・クライアントクラッシュ(フロント/バックエンド型不一致)という
 * 4種類の実バグが手動デバッグでしか発見できなかったため、機械的に回帰検出できるようにする。
 * メール確認機能の追加後は未確認ユーザーの投稿分析がブロックされるため、MailHogから
 * 確認メールを取得してリンクを踏む工程も検証する。
 */
test("新規登録から投稿分析・保存・CSVエクスポートまでの主要導線が壊れない", async ({ page }) => {
  const uniqueEmail = `e2e-${Date.now()}@example.com`;

  await page.goto("/register");
  await page.fill("#displayName", "E2Eテストユーザー");
  await page.fill("#email", uniqueEmail);
  await page.fill("#password", "password123");
  await page.fill("#passwordConfirm", "password123");
  await page.click('button[type="submit"]');

  // 登録成功後はダッシュボードのトップに遷移する
  await expect(page).toHaveURL("/", { timeout: 15_000 });

  // オンボーディングツアーが初回表示されるため閉じる。モーダルは非同期でマウントされるため、
  // 即座のisVisible()チェックではなく数秒待ってから判定する(表示されなければそのまま進める)。
  await page.getByRole("button", { name: "はじめる" }).click({ timeout: 3_000 }).catch(() => {});

  // メール確認を済ませないと投稿分析がブロックされるため、MailHogから確認リンクを取得して踏む
  const verificationLink = await fetchVerificationLink(uniqueEmail);
  await page.goto(verificationLink);
  await expect(page.getByText("メールアドレスの確認が完了しました")).toBeVisible({ timeout: 10_000 });

  await page.goto("/posts/analyze");
  await page.fill("#url", "https://x.com/example_user/status/1234567890123456789");
  await page.click('button[type="submit"]');

  // AI分析(未設定時はルールベースのフォールバック)が完了し、結果セクションが表示される
  await expect(page.getByText("AI分析結果")).toBeVisible({ timeout: 30_000 });

  const bodyAfterAnalyze = await page.locator("body").innerText();
  expect(bodyAfterAnalyze).not.toContain("client-side exception");
  await expectNoA11yViolations(page);

  await page.click('button:has-text("この分析を保存")');
  await expect(page.getByText("保存済み一覧を見る")).toBeVisible({ timeout: 10_000 });

  await page.goto("/saved", { waitUntil: "networkidle" });
  const savedPageBody = await page.locator("body").innerText();
  expect(savedPageBody).not.toContain("client-side exception");
  await expect(page.getByText("詳細を見る").first()).toBeVisible();

  const [download] = await Promise.all([
    page.waitForEvent("download"),
    page.click('button:has-text("CSVエクスポート")'),
  ]);
  const csvPath = await download.path();
  expect(csvPath).toBeTruthy();
});
