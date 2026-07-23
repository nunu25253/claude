package com.buzzanalysis.presentation.exception;

import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.infrastructure.quota.UsageQuotaService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBusinessRule_leavesCodeNull_whenExceptionHasNoErrorCode() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/posts/analyze");

        ResponseEntity<ErrorResponse> response =
                handler.handleBusinessRule(new BusinessRuleViolationException("重複しています"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isNull();
        assertThat(response.getBody().message()).isEqualTo("重複しています");
    }

    @Test
    void handleBusinessRule_propagatesErrorCode_whenExceptionHasOne() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/proposals/generate");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessRule(
                new BusinessRuleViolationException("本日のAI機能の利用回数上限に達しました。明日以降に再度お試しください。",
                        UsageQuotaService.EXCEEDED_ERROR_CODE),
                request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("AI_USAGE_QUOTA_EXCEEDED");
    }
}
