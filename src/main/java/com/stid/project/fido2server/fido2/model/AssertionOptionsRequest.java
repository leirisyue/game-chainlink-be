package com.stid.project.fido2server.fido2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webauthn4j.data.UserVerificationRequirement;
import com.webauthn4j.data.extension.client.AuthenticationExtensionClientInput;
import com.webauthn4j.data.extension.client.AuthenticationExtensionsClientInputs;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssertionOptionsRequest {
    private String username;
    @Schema(allowableValues = {"discouraged", "preferred", "required"})
    private UserVerificationRequirement userVerification = UserVerificationRequirement.DISCOURAGED;
    @JsonIgnoreProperties("hmacGetSecret")
    private AuthenticationExtensionsClientInputs<AuthenticationExtensionClientInput> extensions;
}
