package com.soaesps.schedulerservice.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "public.soa_esps.event_audit")
public class EventReport {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "start_from", nullable = false)
    private LocalDateTime startFrom;

    @Column(name = "end_to", nullable = false)
    private LocalDateTime endTo;

    public EventReport() {}

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public LocalDateTime getStartFrom() {
        return startFrom;
    }

    public void setStartFrom(final LocalDateTime startFrom) {
        this.startFrom = startFrom;
    }

    public LocalDateTime getEndTo() {
        return endTo;
    }

    public void setEndTo(final LocalDateTime endTo) {
        this.endTo = endTo;
    }
}