package com.stid.project.fido2server.app.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.stid.project.fido2server.fido2.service.IFido2ServerChallengeManager;
import com.stid.project.fido2server.fido2.service.ServerChallenge;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.validator.exception.BadChallengeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ServerChallengeCacheService implements IFido2ServerChallengeManager {
    public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final Cache<Challenge, ServerChallenge> serverChallengeCache = CacheBuilder.newBuilder()
            .recordStats()
            .expireAfterWrite(Duration.ofSeconds(120))
            .build();

    public long getCacheCounter() {
        return serverChallengeCache.size();
    }

    @Override
    public Challenge encodeChallenge(ServerChallenge serverChallenge) {
        DefaultChallenge challenge = new DefaultChallenge();
        serverChallengeCache.put(challenge, serverChallenge);
        return challenge;
    }

    @Override
    public ServerChallenge decodeChallenge(Challenge challenge) {
        ServerChallenge serverChallenge = serverChallengeCache.getIfPresent(challenge);
        if (serverChallenge == null)
            throw new BadChallengeException("The actual challenge does not match the expected challenge");
        serverChallengeCache.invalidate(challenge);
        return serverChallenge;
    }
}
