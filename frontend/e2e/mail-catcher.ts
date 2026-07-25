const MAILHOG_BASE_URL = process.env.MAILHOG_BASE_URL ?? "http://localhost:8025";

/**
 * メールキャッチャー(docker-compose上のMailHog、またはDocker不要のe2e/fake-smtp-server.mjs)から
 * 指定アドレス宛の最新メールを取得し、本文中の確認リンク(http(s)://.../verify-email?token=...)を
 * 抽出する。fake-smtp-server.mjsはMailHogの`/api/v2/search`と同じレスポンス形状を返すため、
 * どちらのキャッチャーを使っていてもこの関数は変更不要(playwright.config.tsのwebServerが
 * 既存のMailHogを検出すればそちらを再利用し、無ければfake-smtp-server.mjsを自動起動する)。
 * メール確認機能の追加により /posts/analyze が未確認ユーザーをブロックするようになったため、
 * E2Eの主要導線テストでも実際に確認メールを受信・突破する必要がある。
 */
export async function fetchVerificationLink(email: string): Promise<string> {
  const deadline = Date.now() + 15_000;
  while (Date.now() < deadline) {
    const res = await fetch(
      `${MAILHOG_BASE_URL}/api/v2/search?kind=to&query=${encodeURIComponent(email)}`,
    );
    if (res.ok) {
      const data = (await res.json()) as {
        items: { Content: { Body: string; Headers: Record<string, string[]> } }[];
      };
      const message = data.items?.[0];
      if (message) {
        const encoding = message.Content.Headers["Content-Transfer-Encoding"]?.[0];
        const rawBody = message.Content.Body;
        const body =
          encoding === "base64"
            ? Buffer.from(rawBody.replace(/\r?\n/g, ""), "base64").toString("utf-8")
            : rawBody;
        const match = body.match(/https?:\/\/\S*\/verify-email\?token=\S+/);
        if (match) return match[0];
      }
    }
    await new Promise((resolve) => setTimeout(resolve, 500));
  }
  throw new Error(`確認メールが見つかりませんでした: ${email}`);
}
