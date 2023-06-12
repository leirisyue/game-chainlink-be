package com.stid.project.fido2server.app.config;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Set;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppProperties.class);

    @NotNull
    private String name;
    @NotNull
    private String version;
    @NotNull
    private JwtToken jwtToken;
    private Metadata metadata;

    @Getter
    @Setter
    public static class JwtToken {
        @NotNull
        private String secretKey;
        private Duration tokenValidity = Duration.ofHours(1);
        private Duration tokenValidityRemember = Duration.ofDays(1);
    }

    @Getter
    @Setter
    public static class Metadata {
        private Set<String> mds3Endpoints;
    }
}
