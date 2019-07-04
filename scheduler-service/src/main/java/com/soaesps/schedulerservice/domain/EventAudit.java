package com.soaesps.schedulerservice.domain;

import com.soaesps.core.Utils.convertor.hibernate.TimestampConverter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Table(name = "soa_esps.event_audit")
public class EventAudit {
    @Id
    @GenericGenerator(name="kaugen" , strategy="increment")
    @GeneratedValue(generator="kaugen")
    @Column(name = "id", nullable = false)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_status", nullable = false)
    private EventStatus eventStatus;

    @Column(name = "last_update")//, columnDefinition= "TIMESTAMP WITH TIME ZONE"
    ///@Type(type="java.time.ZonedDateTime")
    @Convert(converter = TimestampConverter.class)
    private ZonedDateTime timestamp;

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

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final ZonedDateTime timestamp) {
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EventAudit [");
        builder.append("id: ").append(id).append(", \n");
        builder.append(eventStatus.toString()).append(", \n");
        builder.append(timestamp.toString()).append("\n");
        return builder.toString();
    }
}