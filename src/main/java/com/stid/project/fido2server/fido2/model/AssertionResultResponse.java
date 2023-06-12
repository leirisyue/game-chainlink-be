package com.stid.project.fido2server.fido2.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stid.project.fido2server.app.domain.entity.Authenticator;
import com.webauthn4j.data.AuthenticatorTransport;
import com.webauthn4j.util.Base64UrlUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class AssertionResultResponse extends ServerResponseBase {
    @JsonIgnore
    private Authenticator authenticator;

    public UUID getId() {
        return authenticator.getId();
    }

    public String getName() {
        return authenticator.getName();
    }

    public String getCredentialId() {
        return Base64UrlUtil.encodeToString(authenticator.getCredentialId());
    }

    public UUID getAaguid() {
        if (authenticator.getAttestedCredentialData() != null) {
            return authenticator.getAttestedCredentialData().getAaguid().getValue();
        }
        return null;
    }

    public String getFormat() {
        return authenticator.getAttestationStatement().getFormat();
    }

    public Set<AuthenticatorTransport> getTransports() {
        return authenticator.getAuthenticatorTransports();
    }

    public Instant getCreatedDate() {
        return authenticator.getCreatedDate();
    }

    public Instant getLastAccess() {
        return authenticator.getLastAccess();
    }

    public long getCounter() {
        return authenticator.getCounter();
    }

    public UUID getUserId() {
        return authenticator.getUserId();
    }

    public String getUsername() {
        return authenticator.getUsername();
    }
}
