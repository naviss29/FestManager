package com.festmanager.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("FestManager API")
                .description("""
                    API REST de gestion de bénévoles et logistique pour festivals et événements culturels.

                    **Authentification** : JWT Bearer — obtenir un token via `POST /api/auth/login`, \
                    puis l'inclure dans les requêtes : `Authorization: Bearer <token>`.

                    **Endpoints publics** : `/api/auth/**` et `POST /api/benevoles/inscription`.

                    **Rôles** : `ADMIN` · `ORGANISATEUR` · `REFERENT_ORGANISATION` · `BENEVOLE`
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("Alan")
                    .email("contact@festmanager.app")
                    .url("https://github.com/naviss29/FestManager"))
                .license(new License()
                    .name("Open Source")
                    .url("https://github.com/naviss29/FestManager")))
            .addSecurityItem(new SecurityRequirement().addList("JWT"))
            .components(new Components()
                .addSecuritySchemes("JWT", new SecurityScheme()
                    .name("JWT")
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Token JWT obtenu via POST /api/auth/login")));
    }
}
