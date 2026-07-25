package com.buzzanalysis.infrastructure.security;

import com.buzzanalysis.infrastructure.logging.RequestIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * JWTベースのステートレス認証を行うSpring Security設定。
 * 認証系エンドポイントとSwagger UI/Actuatorヘルスチェックのみ匿名アクセスを許可し、それ以外はJWT必須とする。
 * {@code /api/v1/reports/files/**}（LocalFileStorageServiceのローカルストレージ配信先）も、
 * S3の署名付きURLと同じ考え方でURLの知得自体をアクセス権とみなし匿名アクセスを許可する。
 * フロントエンド（Next.js）はブラウザから別オリジンでAPIを呼び出すため、CORSを明示的に許可する
 * （{@link CorsProperties}、既定は{@code http://localhost:3000}）。
 *
 * <p>認証トークンをHttpOnly Cookieで送るようになったため（ブラウザが自動送信する）、
 * CSRF対策として{@link CookieCsrfTokenRepository}によるダブルサブミットCookie方式を有効化する
 * （{@code XSRF-TOKEN}Cookieの値をフロントがJSで読み取り{@code X-XSRF-TOKEN}ヘッダーに載せて送り返す。
 * クロスサイトの攻撃者ページは同一オリジンポリシーによりこのCookieの値を読めないため偽造できない）。
 * 認証不要パス({@code PUBLIC_PATHS})はCSRF検証の対象外とする（ログイン等、既存の認証Cookieに依存した
 * 状態変更を行わないため実害が小さく、チキンアンドエッグ問題も避けられる）。GET等の安全なメソッドは
 * Spring Securityの既定動作としてそもそもCSRF検証対象外。
 *
 * <p>Spring Security 6の既定{@code CsrfTokenRequestHandler}はBREACH攻撃対策としてトークンを
 * XORでマスクする({@code XorCsrfTokenRequestAttributeHandler}）。これはサーバーサイドレンダリングの
 * フォーム（リクエストごとに{@code _csrf}隠しフィールドを都度生成）を前提とした対策であり、
 * 「Cookieの値をそのままヘッダーに複製する」という今回のSPA向けダブルサブミットCookie方式とは
 * 相性が悪く、正しいトークンを送っても検証に失敗する。そのため生のトークン値をそのまま扱う
 * {@link CsrfTokenRequestAttributeHandler}に明示的に切り替える
 * （Spring Security公式ドキュメント「Integrating CSRF with Single Page Applications」で
 * 推奨されている構成）。
 *
 * <p>{@link NullAuthenticatedSessionStrategy}を明示的に指定している理由: {@code csrf()}は
 * デフォルトで{@link org.springframework.security.web.csrf.CsrfAuthenticationStrategy}を
 * {@code SessionManagementFilter}のセッション認証ストラテジーチェーンへ自動登録する
 * （{@code CsrfConfigurer#getSessionAuthenticationStrategy()}）。このストラテジーは
 * 「新しい認証が確立された」と判定するたびに、既存のCSRFトークンCookieを一度削除して
 * 再発行する（セッション固定化攻撃対策）。ところが本アプリはSTATELESS
 * （{@code SecurityContextRepository}を使わずCookieのJWTを毎回検証するのみ）のため、
 * 「前回はどの認証だったか」を比較する対象が存在せず、認証済みリクエストは常に
 * 「新しい認証」とみなされてしまう。結果、認証済みGETを1件でも挟むとXSRF-TOKEN Cookieが
 * 削除され、フロントエンドは状態変更リクエストの直前に{@code /auth/csrf}を叩いて
 * 明示的に再取得する回避策を取っていた（実測は行っていたが、Spring Security内部の
 * 原因は特定できていなかった）。実際にバックエンドを起動し
 * {@code logging.level.org.springframework.security=DEBUG}で追跡した結果、認証済み
 * GETのたびに{@code CsrfAuthenticationStrategy - Replaced CSRF Token}が出力されることを
 * 確認して特定した。セッションを使わない設計では上記の固定化対策自体が意味を持たないため、
 * 何もしない{@link NullAuthenticatedSessionStrategy}に差し替えてこの動作を無効化する
 * （フロントエンド側の再取得ロジックは、この修正後も安全側の防御としてそのまま残す）。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 未認証でアクセス可能なパス。以前は"/api/v1/auth/**"を丸ごとpermitAllにしていたため、
     * 実際には認証必須の{@code DELETE /auth/account}や{@code GET /auth/account/export}まで
     * 未認証で到達可能になり、コントローラ内の{@code Authentication.getName()}呼び出しが
     * 想定外の値でUUID変換に失敗して401ではなく400を返してしまっていた(レビューで発覚)。
     * 認証不要な個別パスのみを列挙する形に絞り込む。
     */
    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/api/v1/auth/csrf",
            "/api/v1/auth/password-reset/**",
            "/api/v1/auth/email-verification/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/health",
            "/api/v1/reports/files/**"
    };

    private final JwtTokenProvider jwtTokenProvider;
    private final CorsProperties corsProperties;
    private final RateLimitProperties rateLimitProperties;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider, CorsProperties corsProperties,
                           RateLimitProperties rateLimitProperties, StringRedisTemplate redisTemplate,
                           ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.corsProperties = corsProperties;
        this.rateLimitProperties = rateLimitProperties;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        RequestIdFilter requestIdFilter = new RequestIdFilter();
        RateLimitFilter rateLimitFilter = new RateLimitFilter(redisTemplate, rateLimitProperties, objectMapper);
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider);
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy())
                        .ignoringRequestMatchers(PUBLIC_PATHS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new RestAuthenticationEntryPoint(objectMapper))
                        .accessDeniedHandler(new RestAccessDeniedHandler(objectMapper)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class)
                .addFilterBefore(requestIdFilter, RateLimitFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
