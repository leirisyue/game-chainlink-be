package com.stid.project.fido2server.app.service;

import com.stid.project.fido2server.app.domain.constant.EventName;
import com.stid.project.fido2server.app.domain.constant.EventStatus;
import com.stid.project.fido2server.app.domain.constant.EventType;
import com.stid.project.fido2server.app.domain.entity.Event;
import com.stid.project.fido2server.app.exception.AbstractExceptionHandler;
import com.stid.project.fido2server.app.repository.EventRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class EventService extends AbstractExceptionHandler {
    private final EventRepository eventRepository;
    private final ThreadLocal<Event> threadLocalEvent = new ThreadLocal<>();

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void logEvent(UUID relyingPartyId,
                         EventName eventName, EventType eventType,
                         String eventObject) {
        logEvent(relyingPartyId, null, eventName, eventType, eventObject);
    }

    public void logEvent(UUID relyingPartyId, UUID userId,
                         EventName eventName, EventType eventType,
                         String eventObject) {
        logEvent(relyingPartyId, userId, null, eventName, eventType, eventObject);
    }

    public void logEvent(UUID relyingPartyId, UUID userId, UUID authenticatorId,
                         EventName eventName, EventType eventType,
                         String eventObject) {

        Event event = new Event();
        event.setEventName(eventName);
        event.setEventType(eventType);
        event.setEventObject(eventObject);
        event.setAuthenticatorId(authenticatorId);
        event.setUserId(userId);
        event.setRelyingPartyId(relyingPartyId);
        threadLocalEvent.set(event);
    }

    public void saveEvent() {
        saveEvent(null);
    }

    public void saveEvent(String eventDetail) {
        saveEvent(EventStatus.SUCCESS, eventDetail);
    }

    public void saveEvent(EventStatus eventStatus, String eventDetail) {
        Event event = threadLocalEvent.get();
        if (event != null) {
            event.setEventDetail(eventDetail);
            event.setEventStatus(eventStatus);
            event.setTimestamp(Instant.now());
            saveEventAsync(event);
            threadLocalEvent.remove();
        }
    }

    @Async
    private void saveEventAsync(Event event) {
        eventRepository.save(event);
        LOGGER.info("Saved {}", event);
    }

    public List<Event> findAllByRelyingParty(UUID relyingPartyId, Instant timestampStart, Instant timestampEnd) {
        return eventRepository.findByRelyingPartyIdAndTimestampBetween(relyingPartyId, timestampStart, timestampEnd);
    }

    public List<Event> findAllByRelyingParty(UUID relyingPartyId, EventName eventName, Instant timestampStart, Instant timestampEnd) {
        return eventRepository.findByRelyingPartyIdAndEventNameAndTimestampBetween(relyingPartyId, eventName, timestampStart, timestampEnd);
    }

    public List<Event> findAllByRelyingParty(UUID relyingPartyId, EventType eventType, Instant timestampStart, Instant timestampEnd) {
        return eventRepository.findByRelyingPartyIdAndEventTypeAndTimestampBetween(relyingPartyId, eventType, timestampStart, timestampEnd);
    }

    public List<Event> findAllByRelyingParty(UUID relyingPartyId, UUID userId, Instant timestampStart, Instant timestampEnd) {
        return eventRepository.findByRelyingPartyIdAndUserIdAndTimestampBetween(relyingPartyId, userId, timestampStart, timestampEnd);
    }

    public List<Event> findAllByUser(UUID userId, Instant timestampStart, Instant timestampEnd) {
        return eventRepository.findByUserIdAndTimestampBetween(userId, timestampStart, timestampEnd);
    }

    public List<Event> findAllByAuthenticator(UUID authenticatorId, Instant timestampStart, Instant timestampEnd) {
        return eventRepository.findByAuthenticatorIdAndTimestampBetween(authenticatorId, timestampStart, timestampEnd);
    }
}
