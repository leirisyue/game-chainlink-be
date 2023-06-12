package com.stid.project.fido2server.fido2.service;

import com.stid.project.fido2server.app.domain.entity.RelyingParty;
import com.stid.project.fido2server.app.domain.entity.UserAccount;
import com.stid.project.fido2server.fido2.model.AttestationOptionsRequest;
import com.stid.project.fido2server.fido2.model.AttestationOptionsResponse;
import com.stid.project.fido2server.fido2.model.ServerPublicKeyCredentialDescriptor;
import com.stid.project.fido2server.fido2.util.ServerParameterUtil;
import com.webauthn4j.data.AuthenticatorSelectionCriteria;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.util.Base64UrlUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Fido2ServerAttestationOptionsEndpointService extends AbstractFido2ServerOptionEndpointService {

    protected Fido2ServerAttestationOptionsEndpointService(Fido2ServerChallengeManager challengeManager, Fido2ServerUserAccountManager endpointService) {
        super(challengeManager, endpointService);
    }

    public AttestationOptionsResponse processRequest(
            AttestationOptionsRequest request, RelyingParty relyingParty) {
        //NOTE: find or create new user
        UserAccount userAccount = accountManager.findOrCreateNewUserAccount(
                request.getUsername(), request.getDisplayName(), relyingParty.getId());

        //NOTE: find user authenticators
        List<ServerPublicKeyCredentialDescriptor> excludeCredentials = accountManager
                .findAllUserCredentialDescriptor(userAccount.getId());

        //NOTE: generate challenge
        AuthenticatorSelectionCriteria authenticatorSelection = request.getAuthenticatorSelection();
        ServerChallenge serverChallenge = ServerChallenge.create(
                relyingParty.getRpId(),
                request.getUsername(),
                authenticatorSelection != null ? authenticatorSelection.getUserVerification() : null);
        Challenge challenge = challengeManager.getAttestationService().encodeChallenge(serverChallenge);

        return new AttestationOptionsResponse(
                relyingParty.toRpEntity(),
                userAccount.toServerUserEntity(),
                Base64UrlUtil.encodeToString(challenge.getValue()),
                ServerParameterUtil.getPublicKeyCredentialParameters(),
                ServerParameterUtil.getTimeout(),
                excludeCredentials,
                authenticatorSelection,
                request.getAttestation(),
                request.getExtensions());
    }
}
