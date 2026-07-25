// Docker/Goが使えないサンドボックス環境でもE2E(core-flow.spec.ts)のメール確認導線を
// 検証できるようにするための、MailHogの代替となる最小限のSMTP受信サーバー。
//
// バックエンド(SmtpMailSenderAdapter)はSpring MailのJavaMailSenderで平文SMTP(認証・TLS無し)
// でメールを送信するだけなので、それを受け取れるSMTPリスナーと、MailHogの
// `GET /api/v2/search?kind=to&query=<email>` と同じレスポンス形状を返すHTTP APIさえあれば
// e2e/mail-catcher.tsは変更なしでそのまま使える。
//
// ポート番号はdocker-compose.ymlのMailHogサービスと同じ環境変数・デフォルト値
// (MAIL_SMTP_PORT=1025, MAIL_UI_PORT=8025)を使うため、docker-compose環境と行き来しても
// 設定を変える必要がない。
import { createServer as createHttpServer } from "node:http";
import { SMTPServer } from "smtp-server";

const SMTP_PORT = Number(process.env.MAIL_SMTP_PORT ?? 1025);
const HTTP_PORT = Number(process.env.MAIL_UI_PORT ?? 8025);

// 受信メールアドレス(小文字化)ごとに最新の1通のみ保持する。
// MailHogのように全履歴を保持する必要はなく、core-flow.spec.tsは実行のたびに
// タイムスタンプ付きのユニークなメールアドレスを使うため、宛先ごとに最新1通で十分。
const inboxByRecipient = new Map();

function parseMessage(raw) {
  const separatorMatch = raw.match(/\r?\n\r?\n/);
  const headerBlock = separatorMatch ? raw.slice(0, separatorMatch.index) : raw;
  const body = separatorMatch ? raw.slice(separatorMatch.index + separatorMatch[0].length) : "";

  const headers = {};
  let currentHeaderName = null;
  for (const line of headerBlock.split(/\r?\n/)) {
    if (/^[ \t]/.test(line) && currentHeaderName) {
      // 折り返しヘッダー行(継続行)は直前のヘッダー値に連結する
      headers[currentHeaderName][0] += line.trim();
      continue;
    }
    const colonIndex = line.indexOf(":");
    if (colonIndex === -1) continue;
    const name = line.slice(0, colonIndex).trim();
    const value = line.slice(colonIndex + 1).trim();
    headers[name] = [value];
    currentHeaderName = name;
  }

  return { headers, body };
}

const smtpServer = new SMTPServer({
  // ローカル検証専用のため認証・TLSは要求しない(application.ymlのmail.smtp.auth/starttls既定値に合わせる)
  disabledCommands: ["AUTH", "STARTTLS"],
  onData(stream, session, callback) {
    const chunks = [];
    stream.on("data", (chunk) => chunks.push(chunk));
    stream.on("end", () => {
      const raw = Buffer.concat(chunks).toString("utf-8");
      const { headers, body } = parseMessage(raw);
      const message = { Content: { Body: body, Headers: headers } };
      for (const { address } of session.envelope.rcptTo) {
        inboxByRecipient.set(address.toLowerCase(), message);
      }
      callback();
    });
    stream.on("error", callback);
  },
  onRcptTo(_address, _session, callback) {
    callback();
  },
});

smtpServer.on("error", (err) => {
  console.error(`[fake-smtp-server] SMTPサーバーエラー: ${err.message}`);
});

const httpServer = createHttpServer((req, res) => {
  const url = new URL(req.url, `http://localhost:${HTTP_PORT}`);
  if (url.pathname === "/api/v2/search" && url.searchParams.get("kind") === "to") {
    const email = (url.searchParams.get("query") ?? "").toLowerCase();
    const message = inboxByRecipient.get(email);
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(JSON.stringify({ items: message ? [message] : [] }));
    return;
  }
  res.writeHead(404, { "Content-Type": "application/json" });
  res.end(JSON.stringify({ error: "not found" }));
});

smtpServer.listen(SMTP_PORT, () => {
  console.log(`[fake-smtp-server] SMTP待受中 (port ${SMTP_PORT})`);
});

httpServer.listen(HTTP_PORT, () => {
  console.log(`[fake-smtp-server] HTTP検索API待受中 (port ${HTTP_PORT})`);
});

function shutdown() {
  smtpServer.close();
  httpServer.close();
  process.exit(0);
}

process.on("SIGINT", shutdown);
process.on("SIGTERM", shutdown);
