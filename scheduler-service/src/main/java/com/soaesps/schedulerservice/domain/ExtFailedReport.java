package com.soaesps.schedulerservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "SOA_ESPS.EXT_FAILED_REPORT")
public class ExtFailedReport extends FailedReport {
    /*@OneToMany
    @JoinColumn(name = "event_id", nullable = false)
    private Set<FailedEvent> events = new HashSet<>();*/
}