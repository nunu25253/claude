package com.buzzanalysis.application.auth;

/**
 * Bot対策(CAPTCHA)の検証を抽象化するポート。実装(Cloudflare Turnstile等)はinfrastructure層に置く。
 * {@code app.captcha.enabled=false}の環境(既定値。ローカル開発・テスト用)では、実装側が常に
 * 検証成功として扱うことを想定する。
 */
public interface CaptchaVerificationPort {

    /**
     * @param captchaToken クライアントのウィジェットが発行したトークン(未設定時はnull/空文字を許容)
     * @param remoteIp      検証対象クライアントのIP(任意。取得できない場合はnull)
     * @return 検証に成功した場合true
     */
    boolean verify(String captchaToken, String remoteIp);
}
