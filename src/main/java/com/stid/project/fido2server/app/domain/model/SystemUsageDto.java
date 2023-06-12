package com.stid.project.fido2server.app.domain.model;

import java.io.Serializable;

public record SystemUsageDto(long relyingParties, long userAccounts, long userAuthenticators,
                             long createdPackages, long activatedPackages) implements Serializable {
}