// Buzzly PWA用の最小サービスワーカー。
// 目的は「ホーム画面に追加してアプリらしく開ける」ことのみで、オフラインでの分析機能提供は範囲外。
// このため /api/ へのリクエストは一切インターセプトしない(JWT認証・最新データが必要な全リクエストは
// 常にネットワークへ素通しする。誤ってキャッシュすると古い分析結果や認証エラーを返しかねないため)。
const CACHE_NAME = "buzzly-shell-v1";
const APP_SHELL = ["/manifest.webmanifest", "/icon.svg"];

self.addEventListener("install", (event) => {
  event.waitUntil(
    caches
      .open(CACHE_NAME)
      .then((cache) => cache.addAll(APP_SHELL))
      .then(() => self.skipWaiting()),
  );
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches
      .keys()
      .then((keys) => Promise.all(keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key))))
      .then(() => self.clients.claim()),
  );
});

self.addEventListener("fetch", (event) => {
  const url = new URL(event.request.url);

  // APIリクエスト・自オリジン以外はService Workerを介さずそのまま処理する
  if (event.request.method !== "GET" || url.pathname.startsWith("/api/") || url.origin !== self.location.origin) {
    return;
  }

  // アプリシェル(マニフェスト・アイコン)のみキャッシュ優先。それ以外のページ/JSはネットワーク優先とし、
  // オフライン時のみキャッシュ済みシェルにフォールバックする(古いHTML/JSを誤って出し続けないため)。
  if (APP_SHELL.includes(url.pathname)) {
    event.respondWith(caches.match(event.request).then((cached) => cached ?? fetch(event.request)));
    return;
  }

  event.respondWith(
    fetch(event.request).catch(() => caches.match(event.request).then((cached) => cached ?? Response.error())),
  );
});
