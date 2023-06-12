package com.stid.project.fido2server.app.domain.model;

import com.stid.project.fido2server.app.domain.entity.Authenticator;
import com.webauthn4j.data.AuthenticatorTransport;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * A DTO for the {@link Authenticator} entity
 */
public record AuthenticatorDto(UUID id, String name, String credentialId, String aaguid, String coseKey,
                               String format, Set<AuthenticatorTransport> transports,
                               Instant createdDate, Instant lastAccess, long counter) implements Serializable {
}