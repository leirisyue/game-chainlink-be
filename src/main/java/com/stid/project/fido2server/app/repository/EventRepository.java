package com.stid.project.fido2server.app.repository;

import com.stid.project.fido2server.app.domain.constant.EventName;
import com.stid.project.fido2server.app.domain.constant.EventType;
import com.stid.project.fido2server.app.domain.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByRelyingPartyIdAndTimestampBetween(UUID relyingPartyId, Instant timestampStart, Instant timestampEnd);

    List<Event> findByRelyingPartyIdAndEventNameAndTimestampBetween(UUID relyingPartyId, EventName eventName, Instant timestampStart, Instant timestampEnd);

    List<Event> findByRelyingPartyIdAndEventTypeAndTimestampBetween(UUID relyingPartyId, EventType eventType, Instant timestampStart, Instant timestampEnd);

    List<Event> findByRelyingPartyIdAndUserIdAndTimestampBetween(UUID relyingPartyId, UUID userId, Instant timestampStart, Instant timestampEnd);

    List<Event> findByUserIdAndTimestampBetween(UUID userId, Instant timestampStart, Instant timestampEnd);

    List<Event> findByAuthenticatorIdAndTimestampBetween(UUID authenticatorId, Instant timestampStart, Instant timestampEnd);
}