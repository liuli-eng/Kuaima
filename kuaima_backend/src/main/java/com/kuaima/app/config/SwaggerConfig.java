package com.kuaima.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Swagger / OpenAPI 3 文档配置
 * <p>
 * 启动后访问：
 * <ul>
 *   <li>Swagger UI: <a href="http://localhost:8080/swagger-ui/index.html">/swagger-ui/index.html</a></li>
 *   <li>OpenAPI JSON: <a href="http://localhost:8080/v3/api-docs">/v3/api-docs</a></li>
 * </ul>
 * 支持在 Swagger UI 中通过 Bearer Token 调试受 JWT 保护的接口。
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI kuaimaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("快马日结 API 文档")
                        .description("快马日结后端接口文档，包含 Boss/Worker/Admin/WeChat 等模块")
                        .version("0.0.1-SNAPSHOT")
                        .contact(new Contact()
                                .name("快马团队")
                                .email("dev@kuaima.com"))
                        .license(new License()
                                .name("Private")
                                .url("https://kuaima.com")))
                // 全局 JWT Bearer 认证：在 Knife4j 文档页面右上角 Authorize 输入 token 后，所有接口自动带 Authorization: Bearer xxx
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .description("输入 JWT token，无需加 Bearer 前缀，Knife4j 会自动补全")));
    }
}
