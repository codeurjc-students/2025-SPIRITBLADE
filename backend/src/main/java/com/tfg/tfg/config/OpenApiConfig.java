package com.tfg.tfg.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for SPIRITBLADE API documentation.
 * 
 * Access the Swagger UI at: /swagger-ui/index.html
 * Access the OpenAPI spec at: /v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:SPIRITBLADE}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("SPIRITBLADE API")
                        .version("1.0.0")
                        .description("REST API for League of Legends statistics tracking and analysis. " +
                                "This API provides endpoints for user management, summoner data retrieval, " +
                                "match history analysis, and dashboard statistics.")
                        .contact(new Contact()
                                .name("SPIRITBLADE Team")
                                .email("spiritblade@example.com")
                                .url("https://github.com/codeurjc-students/2025-SPIRITBLADE"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("https://localhost")
                                .description("Local development server (HTTPS)")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT authentication token. Obtain from /api/v1/auth/login endpoint")));
    }
}
