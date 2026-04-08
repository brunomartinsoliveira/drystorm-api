package com.drystorm.api.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DryStorm API")
                        .description("API REST da DryStorm — agendamentos e catálogo da loja de roupas dryfit (landing page)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("DryStorm")
                                .url("https://drystorm.com.br")
                                .email("contato@drystorm.com.br")));
    }
}
