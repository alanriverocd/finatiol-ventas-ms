package com.finatiol.ventas.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;

import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        final String securitySchemeName =
                "bearerAuth";

        return new OpenAPI()

                .info(
                        new Info()
                                .title(
                                        "FINATIOL VENTAS API")
                                .version("1.0"))

                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(
                                        securitySchemeName))

                .components(
                        new Components()
                                .addSecuritySchemes(
                                        securitySchemeName,

                                        new SecurityScheme()
                                                .name(
                                                        securitySchemeName)
                                                .type(
                                                        SecurityScheme.Type.HTTP)
                                                .scheme(
                                                        "bearer")
                                                .bearerFormat(
                                                        "JWT")));
    }
}
