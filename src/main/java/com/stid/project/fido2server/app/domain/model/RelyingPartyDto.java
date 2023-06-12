package com.stid.project.fido2server.app.domain.model;

import com.stid.project.fido2server.app.domain.constant.Status;
import com.stid.project.fido2server.app.domain.entity.RelyingParty;
import com.webauthn4j.data.client.Origin;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * A DTO for the {@link RelyingParty} entity
 */
public record RelyingPartyDto(UUID id, String secret, String name, Origin origin, Set<String> subdomains,
                              Set<Integer> ports, String description,
                              Instant createdDate, Status status) implements Serializable {
}