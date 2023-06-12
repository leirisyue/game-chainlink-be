package com.stid.project.fido2server.fido2.model;

import com.google.gson.Gson;
import com.webauthn4j.data.PublicKeyCredentialType;
import com.webauthn4j.data.extension.client.AuthenticationExtensionClientOutput;
import com.webauthn4j.data.extension.client.AuthenticationExtensionsClientOutputs;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssertionResultRequest {
    private static final Gson gson = new Gson();
    private String id;
    private String rawId;
    private PublicKeyCredentialType type;
    private ServerAuthenticatorAssertionResponse response;
    private AuthenticationExtensionsClientOutputs<AuthenticationExtensionClientOutput> clientExtensionResults;

    public ServerPublicKeyCredential<ServerAuthenticatorAssertionResponse> toCredential() {
        return new ServerPublicKeyCredential<>(id, rawId, type, response, gson.toJson(clientExtensionResults));
    }
}
