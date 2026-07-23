import { test, expect } from "@playwright/test";

/**
 * チーム機能(改善計画No.24: マルチアカウント/チーム機能の土台)の主要導線を検証する。
 * OWNERがチームを作成→メンバーを招待→招待されたMEMBERが一覧を閲覧できる→
 * OWNERがメンバーを削除、までを2ユーザー分の別ブラウザコンテキストで確認する。
 */
test("チームの作成・招待・メンバー閲覧・削除が壊れない", async ({ browser }) => {
  const uniqueSuffix = Date.now();
  const ownerEmail = `e2e-team-owner-${uniqueSuffix}@example.com`;
  const memberEmail = `e2e-team-member-${uniqueSuffix}@example.com`;
  const teamName = `E2Eチーム${uniqueSuffix}`;

  const ownerContext = await browser.newContext();
  const memberContext = await browser.newContext();
  const ownerPage = await ownerContext.newPage();
  const memberPage = await memberContext.newPage();

  async function register(page: typeof ownerPage, email: string, displayName: string) {
    await page.goto("/register");
    await page.fill("#displayName", displayName);
    await page.fill("#email", email);
    await page.fill("#password", "password123");
    await page.fill("#passwordConfirm", "password123");
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL("/", { timeout: 15_000 });
    const onboardingButton = page.getByRole("button", { name: "はじめる" });
    if (await onboardingButton.isVisible().catch(() => false)) {
      await onboardingButton.click();
    }
  }

  await register(ownerPage, ownerEmail, "オーナー太郎");
  await register(memberPage, memberEmail, "メンバー次郎");

  await ownerPage.goto("/team", { waitUntil: "networkidle" });
  await ownerPage.fill("#organizationName", teamName);
  await ownerPage.click('button:has-text("作成する")');
  await expect(ownerPage.getByRole("heading", { name: `メンバー管理: ${teamName}` })).toBeVisible({
    timeout: 10_000,
  });
  await ownerPage.fill("#inviteEmail", memberEmail);
  await ownerPage.click('button:has-text("招待する")');
  await expect(ownerPage.getByText("メンバー次郎")).toBeVisible({ timeout: 10_000 });

  await memberPage.goto("/team", { waitUntil: "networkidle" });
  await memberPage.click(`button:has-text("${teamName}")`);
  await expect(memberPage.getByText("オーナー太郎")).toBeVisible({ timeout: 10_000 });
  // MEMBERロールなので招待フォームは表示されない
  await expect(memberPage.locator("#inviteEmail")).toHaveCount(0);

  await ownerPage.click('li:has-text("メンバー次郎") button:has-text("削除")');
  await expect(ownerPage.getByText("メンバー次郎")).toHaveCount(0, { timeout: 10_000 });

  await ownerContext.close();
  await memberContext.close();
});
