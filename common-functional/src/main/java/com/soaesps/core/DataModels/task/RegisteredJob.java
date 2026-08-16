package com.soaesps.core.DataModels.task;

import com.soaesps.core.DataModels.BaseEntity;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_register")
public class RegisteredJob extends BaseEntity {
    @Embedded
    private BaseJobDesc jobDesc;

    private BaseJobDesc getJobDesc() {
        return jobDesc;
    }

    private void setJobDesc(BaseJobDesc jobDesc) {
        this.jobDesc = jobDesc;
    }
}