package com.stid.project.fido2server.fido2.service;

import com.stid.project.fido2server.app.domain.entity.RelyingParty;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.AttestationObjectConverter;
import com.webauthn4j.converter.CollectedClientDataConverter;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.server.ServerProperty;

public abstract class AbstractFido2ServerResultEndpointService {
    protected final WebAuthnManager webAuthnManager;
    protected final Fido2ServerAuthenticatorManager authenticatorManager;
    protected final Fido2ServerChallengeManager challengeManager;
    protected final Fido2ServerUserAccountManager accountManager;
    protected final CollectedClientDataConverter collectedClientDataConverter;
    protected final AttestationObjectConverter attestationObjectConverter;

    protected AbstractFido2ServerResultEndpointService(WebAuthnManager webAuthnManager, Fido2ServerAuthenticatorManager authenticatorManager, Fido2ServerChallengeManager challengeManager, Fido2ServerUserAccountManager accountManager, CollectedClientDataConverter collectedClientDataConverter, AttestationObjectConverter attestationObjectConverter) {
        this.webAuthnManager = webAuthnManager;
        this.authenticatorManager = authenticatorManager;
        this.challengeManager = challengeManager;
        this.accountManager = accountManager;
        this.collectedClientDataConverter = collectedClientDataConverter;
        this.attestationObjectConverter = attestationObjectConverter;
    }

    protected ServerProperty getServerProperty(RelyingParty relyingParty, Challenge challenge) {
        return new ServerProperty(
                relyingParty.getOrigins(),
                relyingParty.getRpId(),
                challenge,
                null
        );
    }
}
