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
import com.stid.project.fido2server.fido2.model.AssertionResultResponse;
import com.stid.project.fido2server.fido2.model.ServerAuthenticatorAssertionResponse;
import com.stid.project.fido2server.fido2.model.ServerPublicKeyCredential;
import com.stid.project.fido2server.fido2.validator.ServerPublicKeyCredentialValidator;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.authenticator.AuthenticatorImpl;
import com.webauthn4j.converter.AttestationObjectConverter;
import com.webauthn4j.converter.CollectedClientDataConverter;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.AuthenticationRequest;
import com.webauthn4j.data.UserVerificationRequirement;
import com.webauthn4j.data.client.CollectedClientData;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.util.Base64UrlUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

@Service
public class Fido2ServerAssertionResultEndpointService extends AbstractFido2ServerResultEndpointService {
    private final ServerPublicKeyCredentialValidator<ServerAuthenticatorAssertionResponse> serverPublicKeyCredentialValidator = new ServerPublicKeyCredentialValidator<>();

    protected Fido2ServerAssertionResultEndpointService(WebAuthnManager webAuthnManager, Fido2ServerAuthenticatorManager authenticatorManager, Fido2ServerChallengeManager challengeManager, Fido2ServerUserAccountManager endpointService, CollectedClientDataConverter collectedClientDataConverter, AttestationObjectConverter attestationObjectConverter) {
        super(webAuthnManager, authenticatorManager, challengeManager, endpointService, collectedClientDataConverter, attestationObjectConverter);
    }

    public AssertionResultResponse attemptAuthentication(
            ServerPublicKeyCredential<ServerAuthenticatorAssertionResponse> request, RelyingParty relyingParty) {
        serverPublicKeyCredentialValidator.validate(request);

        ServerAuthenticatorAssertionResponse assertionResponse = request.getResponse();
        assertionResponse.validate();

        CollectedClientData collectedClientData = collectedClientDataConverter.convert(assertionResponse.getClientDataJSON());
        Assert.notNull(collectedClientData, "CollectedClientData could not be null");

        Challenge challenge = collectedClientData.getChallenge();
        ServerChallenge serverChallenge = challengeManager.getAssertionService().decodeChallenge(challenge);
        UserVerificationRequirement userVerificationRequirement = serverChallenge.getVerification();

        AuthenticationRequest authenticationRequest = createAuthenticationRequest(request.getRawId(), assertionResponse);

        Authenticator authenticator = authenticatorManager.loadAuthenticatorByCredentialId(authenticationRequest.getCredentialId());
        if (serverChallenge.getUsername() != null && !Objects.equals(serverChallenge.getUsername(), authenticator.getUsername())) {
            System.err.println("TODO: ServerChallenge.Username(" + serverChallenge.getUsername() + ") not equals Authenticator.Username(" + authenticator.getUsername() + ")");
        }

        AuthenticatorImpl authenticatorImpl = authenticator.toImpl();

        //TODO: loadAuthenticatorsByUserHandle
        List<byte[]> allowCredentials = null;
        if (assertionResponse.getUserHandle() != null) {
            byte[] userHandle = Base64UrlUtil.decode(assertionResponse.getUserHandle());
            allowCredentials = authenticatorManager
                    .loadAuthenticatorsByUserHandle(userHandle).stream()
                    .map(Authenticator::getCredentialId)
                    .toList();
        }

        AuthenticationParameters authenticationParameters = createAuthenticationParameters(
                relyingParty,
                challenge,
                authenticatorImpl,
                allowCredentials,
                userVerificationRequirement);
        webAuthnManager.validate(authenticationRequest, authenticationParameters);
        authenticatorManager.updateCounter(authenticator, authenticatorImpl.getCounter());
        return new AssertionResultResponse(authenticator);
    }

    private AuthenticationRequest createAuthenticationRequest(
            String rawId, ServerAuthenticatorAssertionResponse assertionResponse) {
        return new AuthenticationRequest(
                rawId == null ? null : Base64UrlUtil.decode(rawId),
                assertionResponse.getUserHandle() == null ? null : Base64UrlUtil.decode(assertionResponse.getUserHandle()),
                assertionResponse.getAuthenticatorData() == null ? null : Base64UrlUtil.decode(assertionResponse.getAuthenticatorData()),
                assertionResponse.getClientDataJSON() == null ? null : Base64UrlUtil.decode(assertionResponse.getClientDataJSON()),
                assertionResponse.getSignature() == null ? null : Base64UrlUtil.decode(assertionResponse.getSignature()));
    }

    private AuthenticationParameters createAuthenticationParameters(
            RelyingParty relyingParty, Challenge serverChallenge,
            AuthenticatorImpl authenticator, List<byte[]> allowCredentials,
            UserVerificationRequirement userVerification) {
        return new AuthenticationParameters(
                getServerProperty(relyingParty, serverChallenge),
                authenticator,
                allowCredentials,
                userVerification == UserVerificationRequirement.REQUIRED,
                false
        );
    }
}
