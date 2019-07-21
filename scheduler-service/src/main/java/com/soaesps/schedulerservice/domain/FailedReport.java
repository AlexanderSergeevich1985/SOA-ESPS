package com.soaesps.schedulerservice.domain;

import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Immutable
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = "SOA_ESPS.FAILED_REPORT")
@NamedStoredProcedureQueries({
        @NamedStoredProcedureQuery(name = "COMPOSE_REPORT_BY_NAME",
                procedureName = "SOA_ESPS.COMPOSE_REPORT_BY_NAME",
                parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "in", type = String.class)
        }),
        @NamedStoredProcedureQuery(name = "COMPOSE_REPORT_BY_DATE",
                procedureName = "SOA_ESPS.COMPOSE_REPORT_BY_DATE",
                parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "in_1", type = ZonedDateTime.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "in_2", type = ZonedDateTime.class)
        })
})
public class FailedReport extends BaseReport {
    @OneToMany
    @JoinColumn(name = "event_id", nullable = false)
    private Set<FailedEvent> events = new HashSet<>();

    protected FailedReport() {
        super();
    }

    public Set<FailedEvent> getEvents() {
        return events;
    }

    public void setEvents(final Set<FailedEvent> events) {
        this.events = events;
    }
}