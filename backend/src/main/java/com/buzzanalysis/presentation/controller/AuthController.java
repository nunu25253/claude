package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.auth.AuthApplicationService;
import com.buzzanalysis.application.auth.AuthCookieNames;
import com.buzzanalysis.application.auth.EmailVerificationApplicationService;
import com.buzzanalysis.application.auth.PasswordResetApplicationService;
import com.buzzanalysis.application.auth.dto.AuthResult;
import com.buzzanalysis.application.auth.dto.LoginCommand;
import com.buzzanalysis.application.auth.dto.RegisterCommand;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.infrastructure.security.JwtProperties;
import com.buzzanalysis.presentation.dto.request.EmailVerificationConfirmRequest;
import com.buzzanalysis.presentation.dto.request.EmailVerificationResendRequest;
import com.buzzanalysis.presentation.dto.request.LoginRequest;
import com.buzzanalysis.presentation.dto.request.PasswordResetConfirmRequest;
import com.buzzanalysis.presentation.dto.request.PasswordResetRequestRequest;
import com.buzzanalysis.presentation.dto.request.RegisterRequest;
import com.buzzanalysis.presentation.dto.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 認証API（登録/ログイン/トークンリフレッシュ/ログアウト/パスワードリセット/メールアドレス確認）。 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "ユーザー登録・ログイン・トークンリフレッシュ・ログアウト・パスワードリセット・メールアドレス確認")
public class AuthController {

    private final AuthApplicationService authApplicationService;
    private final PasswordResetApplicationService passwordResetApplicationService;
    private final EmailVerificationApplicationService emailVerificationApplicationService;
    private final JwtProperties jwtProperties;

    public AuthController(AuthApplicationService authApplicationService,
                           PasswordResetApplicationService passwordResetApplicationService,
                           EmailVerificationApplicationService emailVerificationApplicationService,
                           JwtProperties jwtProperties) {
        this.authApplicationService = authApplicationService;
        this.passwordResetApplicationService = passwordResetApplicationService;
        this.emailVerificationApplicationService = emailVerificationApplicationService;
        this.jwtProperties = jwtProperties;
    }

    @Operation(summary = "ユーザー登録", description = "成功時、アクセス/リフレッシュトークンをHttpOnly Cookieとして発行する")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                  HttpServletRequest httpRequest) {
        AuthResult result = authApplicationService.register(new RegisterCommand(request.email(), request.password(),
                request.displayName(), request.captchaToken(), httpRequest.getRemoteAddr()));
        return withAuthCookies(HttpStatus.CREATED, result);
    }

    @Operation(summary = "ログイン", description = "成功時、アクセス/リフレッシュトークンをHttpOnly Cookieとして発行する")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = authApplicationService.login(new LoginCommand(request.email(), request.password()));
        return withAuthCookies(HttpStatus.OK, result);
    }

    @Operation(summary = "アクセストークン再発行", description = "リフレッシュトークンはCookieから読み取る(リクエストボディ不要)")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = AuthCookieNames.REFRESH_TOKEN, required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessRuleViolationException("Refresh token cookie is missing");
        }
        AuthResult result = authApplicationService.refresh(refreshToken);
        return withAuthCookies(HttpStatus.OK, result);
    }

    @Operation(summary = "ログアウト", description = "リフレッシュトークンを失効させ、認証Cookieを削除する")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = AuthCookieNames.REFRESH_TOKEN, required = false) String refreshToken) {
        authApplicationService.logout(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearCookie(AuthCookieNames.ACCESS_TOKEN).toString())
                .header(HttpHeaders.SET_COOKIE, clearCookie(AuthCookieNames.REFRESH_TOKEN).toString())
                .build();
    }

    @Operation(summary = "CSRFトークンCookie発行",
            description = "XSRF-TOKEN Cookieを確実に発行するための空エンドポイント。フロントエンドはログイン成功後にこれを一度呼び出す")
    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken csrfToken) {
        // 引数として受け取り明示的にアクセスすることで、Spring Securityの遅延トークン生成を
        // このリクエストの時点で確定させ、レスポンスにXSRF-TOKEN Cookieを発行させる。
        csrfToken.getToken();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "パスワードリセットメール送信依頼",
            description = "指定メールアドレス宛にリセットリンクを送信する。メールが存在しない場合も同じ204を返す(アカウント列挙防止)。")
    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequestRequest request) {
        passwordResetApplicationService.requestReset(request.email());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "パスワードリセット確定", description = "リセットリンクのトークンと新パスワードを受け取り、パスワードを更新する。")
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        passwordResetApplicationService.confirmReset(request.token(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "メールアドレス確認メールの再送依頼",
            description = "指定メールアドレス宛に確認リンクを再送する。メールが存在しない/既に確認済みの場合も同じ204を返す(アカウント列挙防止)。")
    @PostMapping("/email-verification/resend")
    public ResponseEntity<Void> resendEmailVerification(@Valid @RequestBody EmailVerificationResendRequest request) {
        emailVerificationApplicationService.resendVerificationEmail(request.email());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "メールアドレス確認の確定", description = "確認リンクのトークンを受け取り、メールアドレスを確認済みにする。")
    @PostMapping("/email-verification/confirm")
    public ResponseEntity<Void> confirmEmailVerification(@Valid @RequestBody EmailVerificationConfirmRequest request) {
        emailVerificationApplicationService.confirmVerification(request.token());
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<AuthResponse> withAuthCookies(HttpStatus status, AuthResult result) {
        ResponseCookie accessCookie = buildCookie(AuthCookieNames.ACCESS_TOKEN, result.accessToken(),
                result.accessTokenExpiresInSeconds());
        ResponseCookie refreshCookie = buildCookie(AuthCookieNames.REFRESH_TOKEN, result.refreshToken(),
                result.refreshTokenExpiresInSeconds());
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(AuthResponse.from(result));
    }

    private ResponseCookie buildCookie(String name, String value, long maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(jwtProperties.isCookieSecure())
                .sameSite(jwtProperties.getCookieSameSite())
                .path("/")
                .maxAge(maxAgeSeconds);
        applyDomain(builder);
        return builder.build();
    }

    private ResponseCookie clearCookie(String name) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(jwtProperties.isCookieSecure())
                .sameSite(jwtProperties.getCookieSameSite())
                .path("/")
                .maxAge(0);
        applyDomain(builder);
        return builder.build();
    }

    private void applyDomain(ResponseCookie.ResponseCookieBuilder builder) {
        String domain = jwtProperties.getCookieDomain();
        if (domain != null && !domain.isBlank()) {
            builder.domain(domain);
        }
    }
}
