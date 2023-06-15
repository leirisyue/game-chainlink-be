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
public class CustomCreateForm {
    @NotBlank
    private String username;

    @NotBlank
    private String displayName;

    @NotBlank
    private String Password;

    @NotBlank
    private String RePassword;

    @NotNull
    private UUID relyingPartyId;

    private boolean remember;

    private long timeoutInSeconds;
}
