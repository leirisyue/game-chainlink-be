package com.stid.project.fido2server.fido2.service;

import com.webauthn4j.data.client.challenge.Challenge;

public interface IFido2ServerChallengeManager {
    Challenge encodeChallenge(ServerChallenge serverChallenge);

    ServerChallenge decodeChallenge(Challenge challenge);
}