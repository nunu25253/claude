package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.auth.AuthApplicationService;
import com.buzzanalysis.application.auth.EmailVerificationApplicationService;
import com.buzzanalysis.application.auth.PasswordResetApplicationService;
import com.buzzanalysis.application.auth.dto.AuthResult;
import com.buzzanalysis.application.auth.dto.LoginCommand;
import com.buzzanalysis.application.auth.dto.RegisterCommand;
import com.buzzanalysis.presentation.dto.request.EmailVerificationConfirmRequest;
import com.buzzanalysis.presentation.dto.request.EmailVerificationResendRequest;
import com.buzzanalysis.presentation.dto.request.LoginRequest;
import com.buzzanalysis.presentation.dto.request.PasswordResetConfirmRequest;
import com.buzzanalysis.presentation.dto.request.PasswordResetRequestRequest;
import com.buzzanalysis.presentation.dto.request.RefreshRequest;
import com.buzzanalysis.presentation.dto.request.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 認証API（登録/ログイン/トークンリフレッシュ/パスワードリセット/メールアドレス確認）。 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "ユーザー登録・ログイン・トークンリフレッシュ・パスワードリセット・メールアドレス確認")
public class AuthController {

    private final AuthApplicationService authApplicationService;
    private final PasswordResetApplicationService passwordResetApplicationService;
    private final EmailVerificationApplicationService emailVerificationApplicationService;

    public AuthController(AuthApplicationService authApplicationService,
                           PasswordResetApplicationService passwordResetApplicationService,
                           EmailVerificationApplicationService emailVerificationApplicationService) {
        this.authApplicationService = authApplicationService;
        this.passwordResetApplicationService = passwordResetApplicationService;
        this.emailVerificationApplicationService = emailVerificationApplicationService;
    }

    @Operation(summary = "ユーザー登録")
    @PostMapping("/register")
    public ResponseEntity<AuthResult> register(@Valid @RequestBody RegisterRequest request) {
        AuthResult result = authApplicationService.register(
                new RegisterCommand(request.email(), request.password(), request.displayName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(summary = "ログイン")
    @PostMapping("/login")
    public ResponseEntity<AuthResult> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = authApplicationService.login(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "アクセストークン再発行")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResult> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResult result = authApplicationService.refresh(request.refreshToken());
        return ResponseEntity.ok(result);
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
}
