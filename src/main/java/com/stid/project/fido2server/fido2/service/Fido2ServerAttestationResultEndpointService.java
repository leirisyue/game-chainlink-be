/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stid.project.fido2server.fido2.service;

import com.stid.project.fido2server.app.domain.entity.Authenticator;
import com.stid.project.fido2server.app.domain.entity.RelyingParty;
import com.stid.project.fido2server.app.domain.entity.UserAccount;
import com.stid.project.fido2server.fido2.model.AttestationResultResponse;
import com.stid.project.fido2server.fido2.model.ServerAuthenticatorAttestationResponse;
import com.stid.project.fido2server.fido2.model.ServerPublicKeyCredential;
import com.stid.project.fido2server.fido2.util.ServerParameterUtil;
import com.stid.project.fido2server.fido2.validator.ServerPublicKeyCredentialValidator;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.authenticator.AuthenticatorImpl;
import com.webauthn4j.converter.AttestationObjectConverter;
import com.webauthn4j.converter.CollectedClientDataConverter;
import com.webauthn4j.data.*;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.attestation.authenticator.AuthenticatorData;
import com.webauthn4j.data.client.CollectedClientData;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.extension.authenticator.RegistrationExtensionAuthenticatorOutput;
import com.webauthn4j.util.Base64UrlUtil;
import com.webauthn4j.util.exception.WebAuthnException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class Fido2ServerAttestationResultEndpointService extends AbstractFido2ServerResultEndpointService {
    private final ServerPublicKeyCredentialValidator<ServerAuthenticatorAttestationResponse> serverPublicKeyCredentialValidator = new ServerPublicKeyCredentialValidator<>();

    protected Fido2ServerAttestationResultEndpointService(WebAuthnManager webAuthnManager, Fido2ServerAuthenticatorManager authenticatorManager, Fido2ServerChallengeManager challengeManager, Fido2ServerUserAccountManager endpointService, CollectedClientDataConverter collectedClientDataConverter, AttestationObjectConverter attestationObjectConverter) {
        super(webAuthnManager, authenticatorManager, challengeManager, endpointService, collectedClientDataConverter, attestationObjectConverter);
    }

    public AttestationResultResponse processRequest(
            ServerPublicKeyCredential<ServerAuthenticatorAttestationResponse> credential, RelyingParty relyingParty) {
        //NOTE: validate credential
        serverPublicKeyCredentialValidator.validate(credential);

        ServerAuthenticatorAttestationResponse attestationResponse = credential.getResponse();
        CollectedClientData collectedClientData = collectedClientDataConverter.convert(attestationResponse.getClientDataJSON());
        AttestationObject attestationObject = attestationObjectConverter.convert(attestationResponse.getAttestationObject());

        Assert.notNull(collectedClientData, "CollectedClientData could not be null");
        Assert.notNull(attestationObject, "AttestationObject could not be null");

        Assert.notNull(attestationObject.getAuthenticatorData(), "AuthenticatorData could not be null");
        Assert.notNull(attestationObject.getAuthenticatorData().getAttestedCredentialData(), "AttestedCredentialData could not be null");

        AuthenticatorData<RegistrationExtensionAuthenticatorOutput> authenticatorData = attestationObject.getAuthenticatorData();

        Challenge challenge = collectedClientData.getChallenge();
        ServerChallenge serverChallenge = challengeManager.getAttestationService().decodeChallenge(challenge);

        UserAccount userAccount = accountManager.findUserAccount(serverChallenge.getUsername(), relyingParty.getId())
                .orElseThrow(() -> new WebAuthnException("Exception.UserAccountNotFound"));

        RegistrationRequest registrationRequest = createRegistrationRequest(
                attestationResponse.getClientDataJSON(), attestationResponse.getAttestationObject(),
                Collections.emptySet(), credential.getClientExtensionResults());

        RegistrationParameters registrationParameters = createRegistrationParameters(
                relyingParty, challenge, ServerParameterUtil.getPublicKeyCredentialParameters(), serverChallenge.getVerification());

        RegistrationData registrationData = validate(registrationRequest, registrationParameters);

        AuthenticatorImpl authenticatorImpl = AuthenticatorImpl.createFromRegistrationData(registrationData);
        authenticatorImpl.setAuthenticatorExtensions(authenticatorData.getExtensions());
        authenticatorImpl.setClientExtensions(registrationData.getClientExtensions());
        authenticatorImpl.setTransports(registrationData.getTransports());

        Authenticator authenticator = authenticatorManager.createAuthenticator(authenticatorImpl, userAccount.getId());
        return new AttestationResultResponse(authenticator);
    }

    private RegistrationRequest createRegistrationRequest(String clientDataBase64url,
                                                          String attestationObjectBase64url,
                                                          Set<String> transports,
                                                          String clientExtensionsJSON) {
        Assert.hasText(clientDataBase64url, "clientDataBase64url must have text");
        Assert.hasText(attestationObjectBase64url, "attestationObjectBase64url must have text");
        if (transports != null) {
            transports.forEach(transport -> Assert.hasText(transport, "each transport must have text"));
        }
        return new RegistrationRequest(
                Base64UrlUtil.decode(attestationObjectBase64url),
                Base64UrlUtil.decode(clientDataBase64url),
                clientExtensionsJSON,
                transports
        );
    }

    private RegistrationParameters createRegistrationParameters(
            RelyingParty relyingParty, Challenge challenge, List<PublicKeyCredentialParameters> publicKeyCredentialParameters, UserVerificationRequirement verification) {
        return new RegistrationParameters(
                getServerProperty(relyingParty, challenge),
                publicKeyCredentialParameters,
                verification == UserVerificationRequirement.REQUIRED, false
        );
    }

    public RegistrationData validate(
            RegistrationRequest registrationRequest,
            RegistrationParameters registrationParameters) {
        Assert.notNull(registrationRequest, "registrationRequest must not be null");
        Assert.notNull(registrationParameters, "registrationParameters must not be null");
        return webAuthnManager.validate(registrationRequest, registrationParameters);
    }

}
