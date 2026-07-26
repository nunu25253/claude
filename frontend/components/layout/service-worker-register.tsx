"use client";

import { useEffect } from "react";

/**
 * PWAのService Workerをマウント時に登録する。登録失敗(非対応ブラウザ・file://等)は
 * PWAインストール可否に影響するのみでアプリの主要機能とは無関係のため、握りつぶして良い。
 */
export function ServiceWorkerRegister() {
  useEffect(() => {
    if (typeof window === "undefined" || !("serviceWorker" in navigator)) return;
    navigator.serviceWorker.register("/sw.js").catch(() => {});
  }, []);

  return null;
}
