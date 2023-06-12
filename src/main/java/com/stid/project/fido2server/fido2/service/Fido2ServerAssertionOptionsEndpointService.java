package com.stid.project.fido2server.fido2.service;

import com.stid.project.fido2server.app.domain.entity.RelyingParty;
import com.stid.project.fido2server.app.domain.entity.UserAccount;
import com.stid.project.fido2server.app.exception.UnauthorizedException;
import com.stid.project.fido2server.fido2.model.AssertionOptionsRequest;
import com.stid.project.fido2server.fido2.model.AssertionOptionsResponse;
import com.stid.project.fido2server.fido2.model.ServerPublicKeyCredentialDescriptor;
import com.stid.project.fido2server.fido2.util.ServerParameterUtil;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.util.Base64UrlUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class Fido2ServerAssertionOptionsEndpointService extends AbstractFido2ServerOptionEndpointService {
    protected Fido2ServerAssertionOptionsEndpointService(Fido2ServerChallengeManager challengeManager, Fido2ServerUserAccountManager accountManager) {
        super(challengeManager, accountManager);
    }

    public AssertionOptionsResponse processRequest(
            AssertionOptionsRequest request, RelyingParty relyingParty) {
        List<ServerPublicKeyCredentialDescriptor> allowCredentials = new ArrayList<>();
        if (StringUtils.hasText(request.getUsername())) {
            //NOTE: find exist user
            UserAccount userAccount = accountManager.findUserAccount(request.getUsername(), relyingParty.getId())
                    .orElseThrow(() -> new UnauthorizedException("Exception.UserAccountNotFound"));

            //NOTE: find user authenticators
            List<ServerPublicKeyCredentialDescriptor> userCredentialDescriptors = accountManager
                    .findAllUserCredentialDescriptor(userAccount.getId());
            allowCredentials.addAll(userCredentialDescriptors);
        }

        //NOTE: generate challenge
        ServerChallenge serverChallenge = ServerChallenge.create(
                relyingParty.getRpId(),
                request.getUsername(),
                request.getUserVerification());
        Challenge challenge = challengeManager.getAssertionService().encodeChallenge(serverChallenge);

        return new AssertionOptionsResponse(
                Base64UrlUtil.encodeToString(challenge.getValue()),
                ServerParameterUtil.getTimeout(),
                relyingParty.getRpId(),
                allowCredentials,
                request.getUserVerification(),
                request.getExtensions());
    }
}
