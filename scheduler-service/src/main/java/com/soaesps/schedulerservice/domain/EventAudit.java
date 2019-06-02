package com.soaesps.schedulerservice.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "public.soa_esps.event_audit")
public class EventAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_status", nullable = false)
    private EventStatus eventStatus;

    @Column(name = "last_update", nullable = false)
    private LocalDateTime timestamp;

    public EventAudit() {}

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public EventStatus getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(final EventStatus eventStatus) {
        this.eventStatus = eventStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public enum EventStatus {
        REPORTED("REPORTED"),
        SUBMITTED("SUBMITTED");

        private String eventStatus;

        EventStatus(final String eventStatus) {
            this.eventStatus = eventStatus;
        }

        public String getEventStatus() {
            return eventStatus;
        }

        public void setEventStatus(final String eventStatus) {
            this.eventStatus = eventStatus;
        }

        @Override
        public String toString() {
            return "EventStatus [eventStatus=" + this.eventStatus + "]";
        }
    }
}