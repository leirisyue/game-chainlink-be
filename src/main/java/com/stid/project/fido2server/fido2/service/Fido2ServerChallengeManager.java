package com.stid.project.fido2server.fido2.service;

import com.stid.project.fido2server.app.service.ServerChallengeCacheService;
import com.stid.project.fido2server.app.service.ServerChallengeCborService;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Getter
@Service
public class Fido2ServerChallengeManager {
    private final ServerChallengeCborService challengeCborService;
    private final ServerChallengeCacheService challengeCacheService;

    public Fido2ServerChallengeManager(ServerChallengeCborService challengeCborService, ServerChallengeCacheService challengeCacheService) {
        this.challengeCborService = challengeCborService;
        this.challengeCacheService = challengeCacheService;
    }

    public IFido2ServerChallengeManager getAttestationService() {
        return challengeCacheService;
    }

    public IFido2ServerChallengeManager getAssertionService() {
        return challengeCborService;
    }
}
