"use client";

import { useEffect, useRef, useState } from "react";
import { FormField, inputClassName } from "@/components/ui/form-field";
import { Button } from "@/components/ui/button";

interface GmoTokenResponse {
  resultCode: string;
  tokenObject?: { token?: string[] };
}

declare global {
  interface Window {
    Multipayment?: {
      init: (shopId: string) => void;
      getToken: (
        cardInfo: { cardno: string; expire: string; securitycode: string; holdername?: string },
        callback: (response: GmoTokenResponse) => void,
      ) => void;
    };
  }
}

const SCRIPT_SRC = "https://static.mul-pay.jp/ext/js/token.js";
let scriptLoadPromise: Promise<void> | null = null;

// GmoPaymentGatewayAdapter(バックエンド)と同じ理由により、この実装はGMO-PGの一般的な
// マルチペイメントサービスのトークン化JS(token.js)のAPI形状に基づくものであり、実契約での
// 疎通確認は行っていない。本番投入前に、契約時にGMOから提供される技術仕様書と突き合わせて
// 検証すること。Turnstileウィジェットと同じくCSPのscript-src 'strict-dynamic'により、
// このコンポーネント自身のスクリプトがdocument.createElementで動的に挿入するスクリプトは
// 個別のnonce付与無しで信頼される。
function loadGmoTokenScript(): Promise<void> {
  if (typeof window === "undefined") return Promise.resolve();
  if (window.Multipayment) return Promise.resolve();
  if (scriptLoadPromise) return scriptLoadPromise;

  scriptLoadPromise = new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.src = SCRIPT_SRC;
    script.async = true;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error("決済トークン化スクリプトの読み込みに失敗しました"));
    document.head.appendChild(script);
  });
  return scriptLoadPromise;
}

interface GmoCardTokenFormProps {
  onToken: (token: string) => void;
  isSubmitting: boolean;
}

/**
 * カード番号・有効期限・セキュリティコードを画面内で受け取り、GMO-PGのクライアントサイドJSで
 * トークン化した上でonTokenに渡す(生のカード番号はバックエンドへ一切送信しない)。
 *
 * NEXT_PUBLIC_GMO_SHOP_ID未設定の場合(ローカル開発・レビュー環境で実際のGMO契約が無い場合)は、
 * カードトークンを直接入力できる簡易フォームにフォールスクする。この場合バックエンドは
 * MockPaymentGatewayAdapter(任意の文字列で疑似決済成功)が使われる想定(PAYMENT_GATEWAY_PROVIDER
 * の既定値と一致)。
 */
export function GmoCardTokenForm({ onToken, isSubmitting }: GmoCardTokenFormProps) {
  const shopId = process.env.NEXT_PUBLIC_GMO_SHOP_ID;
  const [cardNumber, setCardNumber] = useState("");
  const [expireMonth, setExpireMonth] = useState("");
  const [expireYear, setExpireYear] = useState("");
  const [securityCode, setSecurityCode] = useState("");
  const [manualToken, setManualToken] = useState("");
  const [tokenizing, setTokenizing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const initializedRef = useRef(false);

  useEffect(() => {
    if (!shopId) return;
    loadGmoTokenScript()
      .then(() => {
        if (!initializedRef.current && window.Multipayment) {
          window.Multipayment.init(shopId);
          initializedRef.current = true;
        }
      })
      .catch(() => setError("決済フォームの初期化に失敗しました。時間をおいて再度お試しください。"));
  }, [shopId]);

  if (!shopId) {
    // 開発/評価環境向けフォールバック: 実際のユーザーに晒す本番ビルドではこの分岐は使われない
    // (NEXT_PUBLIC_GMO_SHOP_IDが必ず設定される運用を前提とする)。
    return (
      <form
        onSubmit={(e) => {
          e.preventDefault();
          onToken(manualToken);
        }}
        className="space-y-4"
      >
        <FormField
          label="カードトークン"
          htmlFor="cardToken"
          hint={
            process.env.NODE_ENV !== "production"
              ? "開発/評価環境では任意の文字列で疑似決済が成功します(NEXT_PUBLIC_GMO_SHOP_ID未設定)"
              : undefined
          }
        >
          <input
            id="cardToken"
            placeholder="tok_xxxxxxxx"
            className={inputClassName(false)}
            value={manualToken}
            onChange={(e) => setManualToken(e.target.value)}
          />
        </FormField>
        <Button type="submit" isLoading={isSubmitting} disabled={!manualToken}>
          アップグレードする
        </Button>
      </form>
    );
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!window.Multipayment) {
      setError("決済フォームの準備ができていません。時間をおいて再度お試しください。");
      return;
    }
    setTokenizing(true);
    window.Multipayment.getToken(
      {
        cardno: cardNumber.replace(/\s/g, ""),
        expire: `${expireYear}${expireMonth.padStart(2, "0")}`,
        securitycode: securityCode,
      },
      (response) => {
        setTokenizing(false);
        const token = response.tokenObject?.token?.[0];
        if (response.resultCode !== "000" || !token) {
          setError("カード情報のトークン化に失敗しました。カード番号・有効期限・セキュリティコードをご確認ください。");
          return;
        }
        onToken(token);
      },
    );
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <FormField label="カード番号" htmlFor="cardNumber">
        <input
          id="cardNumber"
          inputMode="numeric"
          autoComplete="cc-number"
          placeholder="4242 4242 4242 4242"
          className={inputClassName(false)}
          value={cardNumber}
          onChange={(e) => setCardNumber(e.target.value)}
        />
      </FormField>
      <div className="grid grid-cols-3 gap-3">
        <FormField label="有効期限(月)" htmlFor="expireMonth">
          <input
            id="expireMonth"
            inputMode="numeric"
            autoComplete="cc-exp-month"
            placeholder="MM"
            maxLength={2}
            className={inputClassName(false)}
            value={expireMonth}
            onChange={(e) => setExpireMonth(e.target.value)}
          />
        </FormField>
        <FormField label="有効期限(年)" htmlFor="expireYear">
          <input
            id="expireYear"
            inputMode="numeric"
            autoComplete="cc-exp-year"
            placeholder="YY"
            maxLength={2}
            className={inputClassName(false)}
            value={expireYear}
            onChange={(e) => setExpireYear(e.target.value)}
          />
        </FormField>
        <FormField label="セキュリティコード" htmlFor="securityCode">
          <input
            id="securityCode"
            inputMode="numeric"
            autoComplete="cc-csc"
            placeholder="123"
            maxLength={4}
            className={inputClassName(false)}
            value={securityCode}
            onChange={(e) => setSecurityCode(e.target.value)}
          />
        </FormField>
      </div>
      {error && (
        <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">
          {error}
        </p>
      )}
      <Button
        type="submit"
        isLoading={isSubmitting || tokenizing}
        disabled={!cardNumber || !expireMonth || !expireYear || !securityCode}
      >
        アップグレードする
      </Button>
    </form>
  );
}
