package com.stid.project.fido2server.app.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
@Builder
public class CustomLoginForm {
    @NotBlank
    private String username;

    @NotBlank
    private String Password;

    @NotNull
    private UUID relyingPartyId;
    private long timeoutInSeconds;
}
