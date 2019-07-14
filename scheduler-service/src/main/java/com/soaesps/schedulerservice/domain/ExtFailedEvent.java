package com.soaesps.schedulerservice.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "SOA_ESPS.EXT_FAILED_EVENT")
public class ExtFailedEvent extends FailedEvent {
}