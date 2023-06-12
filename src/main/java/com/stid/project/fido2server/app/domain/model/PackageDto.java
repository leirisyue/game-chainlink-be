package com.stid.project.fido2server.app.domain.model;

import com.stid.project.fido2server.app.domain.constant.PackageType;
import com.stid.project.fido2server.app.domain.entity.Package;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * A DTO for the {@link Package} entity
 */
public record PackageDto(UUID id, PackageType type, long amount, Instant createdDate, Instant activatedDate,
                         String description) implements Serializable {
}