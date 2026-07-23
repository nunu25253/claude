package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.auth.AuthApplicationService;
import com.buzzanalysis.application.auth.EmailVerificationApplicationService;
import com.buzzanalysis.application.auth.PasswordResetApplicationService;
import com.buzzanalysis.application.auth.dto.AuthResult;
import com.buzzanalysis.application.auth.dto.LoginCommand;
import com.buzzanalysis.application.auth.dto.RegisterCommand;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.infrastructure.security.JwtProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link AuthController} のMockMvcによるAPIテスト。
 * サーブレットフィルタ（Spring Security）はこのスライステストの対象外のため無効化し、
 * コントローラのリクエスト/レスポンスマッピング・Cookie発行ロジックのみを検証する。
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthApplicationService authApplicationService;

    @MockBean
    private PasswordResetApplicationService passwordResetApplicationService;

    @MockBean
    private EmailVerificationApplicationService emailVerificationApplicationService;

    @MockBean
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        when(jwtProperties.isCookieSecure()).thenReturn(false);
        when(jwtProperties.getCookieSameSite()).thenReturn("Lax");
        when(jwtProperties.getCookieDomain()).thenReturn("");
    }

    @Test
    void register_returns201WithAuthCookies_andNoTokenInBody() throws Exception {
        AuthResult mockResult = new AuthResult(UUID.randomUUID(), "new@example.com", "New User", false,
                "access-token-value", 1800, "refresh-token-value", 1209600);
        when(authApplicationService.register(any(RegisterCommand.class))).thenReturn(mockResult);

        String requestBody = """
                {"email":"new@example.com","password":"Password123!","displayName":"New User"}
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(cookie().value("access_token", "access-token-value"))
                .andExpect(cookie().httpOnly("access_token", true))
                .andExpect(cookie().maxAge("access_token", 1800))
                .andExpect(cookie().value("refresh_token", "refresh-token-value"))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().maxAge("refresh_token", 1209600));
    }

    @Test
    void register_returns400_whenEmailIsBlank() throws Exception {
        String requestBody = """
                {"email":"","password":"Password123!","displayName":"New User"}
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void login_returns400_whenCredentialsAreInvalid() throws Exception {
        when(authApplicationService.login(any(LoginCommand.class)))
                .thenThrow(new BusinessRuleViolationException("Invalid email or password"));

        String requestBody = """
                {"email":"user@example.com","password":"wrong-password"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_returns400_whenRefreshTokenCookieIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_returnsNewCookies_whenRefreshTokenCookiePresent() throws Exception {
        AuthResult mockResult = new AuthResult(UUID.randomUUID(), "user@example.com", "User", true,
                "new-access-token", 1800, "new-refresh-token", 1209600);
        when(authApplicationService.refresh("old-refresh-token")).thenReturn(mockResult);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "old-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(cookie().value("access_token", "new-access-token"))
                .andExpect(cookie().value("refresh_token", "new-refresh-token"));
    }

    @Test
    void logout_clearsAuthCookies() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "some-refresh-token")))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("access_token", 0))
                .andExpect(cookie().maxAge("refresh_token", 0));
    }

    @Test
    void logout_succeeds_evenWithoutRefreshTokenCookie() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent());
    }
}
