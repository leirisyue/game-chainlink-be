package com.stid.project.fido2server.app.domain.model;

import com.stid.project.fido2server.app.domain.constant.EventName;
import com.stid.project.fido2server.app.domain.constant.EventStatus;
import com.stid.project.fido2server.app.domain.constant.EventType;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * A DTO for the {@link com.stid.project.fido2server.app.domain.entity.Event} entity
 */
public record EventDto(UUID id, EventName eventName, EventType eventType, EventStatus eventStatus, String eventDetail,
                       Instant timestamp) implements Serializable {
}