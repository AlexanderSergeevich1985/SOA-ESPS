package com.soaesps.schedulerservice.domain;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = "SOA_ESPS.FAILED_EVENT")
public class FailedEvent extends BaseEvent {
    @Embedded
    private BaseJobDesc jobDesc;

    public FailedEvent() {
        super();
    }

    public BaseJobDesc getJobDesc() {
        return jobDesc;
    }

    public void setJobDesc(final BaseJobDesc jobDesc) {
        this.jobDesc = jobDesc;
    }
}