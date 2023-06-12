package com.stid.project.fido2server.app.domain.entity;

import com.stid.project.fido2server.app.domain.constant.EventName;
import com.stid.project.fido2server.app.domain.constant.EventStatus;
import com.stid.project.fido2server.app.domain.constant.EventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tbl_event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_name", length = 200)
    private EventName eventName;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 50)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_status", length = 50)
    private EventStatus eventStatus;

    @Column(name = "event_detail", length = 2000)
    private String eventDetail;

    @Column(name = "event_object")
    private String eventObject;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp = Instant.now();

    @Column(name = "authenticator_id", updatable = false)
    private UUID authenticatorId;

    @Column(name = "user_id", updatable = false)
    private UUID userId;

    @Column(name = "relying_party_id", updatable = false)
    private UUID relyingPartyId;

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", eventName=" + eventName +
                ", eventType=" + eventType +
                ", eventStatus=" + eventStatus +
                ", eventDetail='" + eventDetail + '\'' +
                ", eventObject='" + eventObject + '\'' +
                ", timestamp=" + timestamp +
                ", authenticatorId=" + authenticatorId +
                ", userId=" + userId +
                ", relyingPartyId=" + relyingPartyId +
                '}';
    }
}
