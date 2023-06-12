package com.stid.project.fido2server.fido2.model;

import com.google.gson.Gson;
import com.webauthn4j.data.AuthenticatorAttachment;
import com.webauthn4j.data.PublicKeyCredentialType;
import com.webauthn4j.data.extension.client.AuthenticationExtensionsClientOutputs;
import com.webauthn4j.data.extension.client.RegistrationExtensionClientOutput;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttestationResultRequest {
    private static final Gson gson = new Gson();
    private String id;
    private String rawId;
    private PublicKeyCredentialType type;
    private ServerAuthenticatorAttestationResponse response;
    private AuthenticationExtensionsClientOutputs<RegistrationExtensionClientOutput> clientExtensionResults;

    private AuthenticatorAttachment authenticatorAttachment;

    public ServerPublicKeyCredential<ServerAuthenticatorAttestationResponse> toCredential() {
        return new ServerPublicKeyCredential<>(id, rawId, type, response, gson.toJson(clientExtensionResults));
    }
}
