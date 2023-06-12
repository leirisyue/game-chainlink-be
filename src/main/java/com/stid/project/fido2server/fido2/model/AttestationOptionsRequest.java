package com.stid.project.fido2server.fido2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webauthn4j.data.AttestationConveyancePreference;
import com.webauthn4j.data.AuthenticatorSelectionCriteria;
import com.webauthn4j.data.extension.client.AuthenticationExtensionsClientInputs;
import com.webauthn4j.data.extension.client.RegistrationExtensionClientInput;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperties;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttestationOptionsRequest {
    @NotBlank
    private String username;
    private String displayName;
    @SchemaProperties({
            @SchemaProperty(name = "authenticatorAttachment", schema = @Schema(allowableValues = {"platform", "cross-platform"})),
            @SchemaProperty(name = "residentKey", schema = @Schema(allowableValues = {"discouraged", "preferred", "required"})),
            @SchemaProperty(name = "userVerification", schema = @Schema(allowableValues = {"discouraged", "preferred", "required"}))
    })
    private AuthenticatorSelectionCriteria authenticatorSelection;
    @Schema(allowableValues = {"none", "direct", "indirect", "enterprise"})
    private AttestationConveyancePreference attestation;
    @JsonIgnoreProperties("hmacGetSecret")
    private AuthenticationExtensionsClientInputs<RegistrationExtensionClientInput> extensions;
}
