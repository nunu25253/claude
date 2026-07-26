import { defineConfig, devices } from "@playwright/test";

/**
 * 主要導線(登録→ログイン→投稿分析→保存)のE2Eテスト設定。
 * フロントエンドの自動テストが皆無で、本セッション中に何度も同種のバグ(403/400/500/
 * クライアントクラッシュ)を手動デバッグで発見してきたことへの対応。
 */
export default defineConfig({
  testDir: "./e2e",
  timeout: 60_000,
  expect: { timeout: 10_000 },
  fullyParallel: false,
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI ? "github" : "list",
  use: {
    baseURL: process.env.E2E_BASE_URL ?? "http://localhost:3000",
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
  },
  projects: [
    {
      name: "chromium",
      use: {
        ...devices["Desktop Chrome"],
        // このサンドボックス環境ではPlaywrightのブラウザが/opt/pw-browsersに配置済みのため、
        // 実行環境ごとのダウンロードを避けてそのバイナリを直接指定する。
        launchOptions: process.env.PLAYWRIGHT_CHROMIUM_PATH
          ? { executablePath: process.env.PLAYWRIGHT_CHROMIUM_PATH }
          : {},
      },
    },
  ],
  webServer: [
    {
      command: "npm run dev",
      url: "http://localhost:3000",
      reuseExistingServer: true,
      timeout: 120_000,
    },
    {
      // MailHog(docker-compose)が既に8025で起動していればそれを再利用し、
      // 無ければDocker不要のfake-smtp-server.mjsを自動起動する。どちらも
      // MailHog互換の/api/v2/search APIを返すためe2e/mail-catcher.tsは変更不要。
      command: "npm run e2e:mail-server",
      port: 8025,
      reuseExistingServer: true,
      timeout: 30_000,
    },
  ],
});
