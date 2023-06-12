package com.stid.project.fido2server.fido2.service;

public abstract class AbstractFido2ServerOptionEndpointService {
    protected final Fido2ServerChallengeManager challengeManager;
    protected final Fido2ServerUserAccountManager accountManager;

    protected AbstractFido2ServerOptionEndpointService(Fido2ServerChallengeManager challengeManager, Fido2ServerUserAccountManager accountManager) {
        this.challengeManager = challengeManager;
        this.accountManager = accountManager;
    }
}
