package com.buzzanalysis.infrastructure.security;

import com.buzzanalysis.presentation.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * 認証済みだが権限が無いリクエストへの応答。{@link RestAuthenticationEntryPoint}と対になる
 * (未認証=401はそちら、認証済みだが権限不足=403はこちら)。現時点ではロールベースの
 * 認可ルールが無いため実際に発火する経路は無いが、将来{@code @PreAuthorize}等を追加した際に
 * Spring Securityの既定{@code AccessDeniedHandlerImpl}(ボディ無し403)にフォールバックしないよう、
 * 最初から{@link com.buzzanalysis.presentation.exception.ErrorResponse}形式で統一しておく。
 */
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        ErrorResponse body = ErrorResponse.of(HttpServletResponse.SC_FORBIDDEN, "Forbidden",
                "この操作を行う権限がありません。", request.getRequestURI());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
