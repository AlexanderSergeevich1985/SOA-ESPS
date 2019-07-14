package com.soaesps.schedulerservice.domain;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = "SOA_ESPS.FAILED_EVENT")
public class FailedEvent extends BaseEvent {
}