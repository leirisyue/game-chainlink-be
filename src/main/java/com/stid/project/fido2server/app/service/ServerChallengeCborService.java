package com.stid.project.fido2server.app.service;

import com.stid.project.fido2server.fido2.service.IFido2ServerChallengeManager;
import com.stid.project.fido2server.fido2.service.ServerChallenge;
import com.webauthn4j.converter.util.CborConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.validator.exception.BadChallengeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ServerChallengeCborService implements IFido2ServerChallengeManager {
    public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    protected final CborConverter cborConverter;

    public ServerChallengeCborService(ObjectConverter objectConverter) {
        this.cborConverter = objectConverter.getCborConverter();
    }

    @Override
    public Challenge encodeChallenge(ServerChallenge serverChallenge) {
        byte[] value = cborConverter.writeValueAsBytes(serverChallenge);
        return new DefaultChallenge(value);
    }

    @Override
    public ServerChallenge decodeChallenge(Challenge challenge) {
        try {
            ServerChallenge serverChallenge = cborConverter.readValue(challenge.getValue(), ServerChallenge.class);
            assert serverChallenge != null;
            return serverChallenge;
        } catch (Exception e) {
            throw new BadChallengeException("The actual challenge does not match the expected challenge");
        }
    }
}
