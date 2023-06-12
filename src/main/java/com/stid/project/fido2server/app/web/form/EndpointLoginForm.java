package com.stid.project.fido2server.app.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class EndpointLoginForm {
    @NotNull
    private UUID clientId;

    @NotBlank
    private String clientSecret;

    private long timeoutInSeconds;
}
