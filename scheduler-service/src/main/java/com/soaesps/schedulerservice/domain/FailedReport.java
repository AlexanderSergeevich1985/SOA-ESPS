package com.soaesps.schedulerservice.domain;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = "SOA_ESPS.FAILED_REPORT")
public class FailedReport extends BaseReport {
    @OneToMany
    @JoinColumn(name = "event_id", nullable = false)
    private Set<FailedEvent> events = new HashSet<>();
}