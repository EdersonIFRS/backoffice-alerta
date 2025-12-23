package com.backoffice.alerta.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Define o esquema de segurança JWT Bearer
        final String securitySchemeName = "Bearer Authentication";
        
        return new OpenAPI()
                .info(new Info()
                        .title("API de Análise de Risco de Pull Requests")
                        .version("1.0.0")
                        .description("API REST para análise inteligente de risco de Pull Requests. " +
                                   "A engine infere automaticamente a criticidade dos arquivos e o histórico de incidentes " +
                                   "baseado no caminho dos arquivos alterados.\n\n" +
                                   "**Autenticação:** Use o endpoint /auth/login para obter um token JWT. " +
                                   "Clique no botão 'Authorize' e insira o token no formato: Bearer {token}"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Insira o token JWT obtido no endpoint /auth/login")));
    }
}
