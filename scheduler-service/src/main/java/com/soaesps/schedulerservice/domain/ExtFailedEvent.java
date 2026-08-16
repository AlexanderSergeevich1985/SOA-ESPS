package com.soaesps.schedulerservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "SOA_ESPS.EXT_FAILED_EVENT")
public class ExtFailedEvent extends FailedEvent {
}