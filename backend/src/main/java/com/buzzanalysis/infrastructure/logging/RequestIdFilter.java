package com.buzzanalysis.infrastructure.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * リクエストごとに相関ID(requestId)を発行し、SLF4JのMDCへ設定する。
 * ログのパターン({@code logging.pattern.console})に{@code %X{requestId}}を含めることで、
 * 1リクエストにまたがる全てのログ行を1つのIDで追跡できるようにする(本番障害調査の効率化)。
 * クライアントが{@code X-Request-Id}ヘッダーを送ってきた場合はそれを引き継ぎ、
 * レスポンスにも同じヘッダーを付与するため、フロントのエラー表示と突き合わせることもできる。
 */
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "X-Request-Id";
    private static final String MDC_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = request.getHeader(HEADER_NAME);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        response.setHeader(HEADER_NAME, requestId);
        MDC.put(MDC_KEY, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
