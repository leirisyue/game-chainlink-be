package com.stid.project.fido2server.app.config;

import com.stid.project.fido2server.app.security.CurrentSpringUser;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class OpenAPIConfiguration {
    public static final String SECURITY_SCHEMA_NAME = "JWT";

    private final AppProperties appProperties;

    public OpenAPIConfiguration(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        SpringDocUtils.getConfig()
                .addAnnotationsToIgnore(CurrentSpringUser.class);

        Info info = new Info()
                .title(appProperties.getName())
                .version(appProperties.getVersion());

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .name(HttpHeaders.AUTHORIZATION)
                .in(SecurityScheme.In.HEADER)
                .scheme("bearer")
                .bearerFormat("JWT");

        Components components = new Components();
        components.addSecuritySchemes(SECURITY_SCHEMA_NAME, securityScheme);

        return new OpenAPI(SpecVersion.V31)
                .info(info)
                .components(components)
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEMA_NAME));
    }

}
