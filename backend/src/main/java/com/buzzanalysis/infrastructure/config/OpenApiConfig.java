package com.buzzanalysis.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** springdoc-openapiによるSwagger UI（/swagger-ui.html）向けのAPI定義設定。 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI buzzAnalysisOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SNS AI Buzz Analysis Platform API")
                        .description("Instagram / TikTok / X の公開投稿を対象に、AIがバズった理由を分析するプラットフォームのバックエンドAPI")
                        .version("v1")
                        .contact(new Contact().name("Buzz Analysis Platform Team")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
