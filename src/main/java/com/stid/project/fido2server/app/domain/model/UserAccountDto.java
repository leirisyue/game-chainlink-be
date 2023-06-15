package com.stid.project.fido2server.app.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stid.project.fido2server.app.domain.entity.UserAccount;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * A DTO for the {@link UserAccount} entity
 */
public record UserAccountDto(UUID id, String userHandle, String userLogin, @JsonIgnore String userEmail,
                             String displayName,
                             Instant createdDate, UUID relyingPartyId) implements Serializable {
}