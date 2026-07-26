package com.buzzanalysis.application.auth;

/**
 * 認証Cookieの名前定数。presentation層（{@code AuthController}のCookie発行/削除）と
 * infrastructure層（{@code JwtAuthenticationFilter}のCookie読み取り）の両方から参照するため、
 * 両者が依存できるapplication層に置く。
 */
public final class AuthCookieNames {

    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";

    private AuthCookieNames() {
    }
}
