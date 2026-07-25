package com.buzzanalysis.infrastructure.security;

import com.buzzanalysis.presentation.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * 未認証リクエストへの応答。Spring Securityの{@code ExceptionTranslationFilter}は
 * フィルタ段階で例外を捕捉するため{@link com.buzzanalysis.presentation.exception.GlobalExceptionHandler}
 * (DispatcherServletより後段の{@code @RestControllerAdvice})には到達しない。カスタムの
 * {@link AuthenticationEntryPoint}を明示しないとSpring Securityの既定{@code Http403ForbiddenEntryPoint}
 * が使われ、本来401であるべき「未認証」がボディ無しの403として返ってしまう
 * (レビューで発覚。認可はあるが認証が無いケースと、認証済みだが権限が無いケースを
 * HTTPステータスで区別できていなかった)。GlobalExceptionHandlerと同じ{@link ErrorResponse}
 * 形式で401を返すことで、フロントエンド・クライアント側のエラーハンドリングを一貫させる。
 */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        ErrorResponse body = ErrorResponse.of(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized",
                "認証が必要です。ログインしてください。", request.getRequestURI());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
