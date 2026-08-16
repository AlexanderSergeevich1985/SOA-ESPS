package com.soaesps.core.DataModels.executor;

import com.soaesps.core.DataModels.task.RegisteredJob;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ref_node_job")
public class NodeJobRegister {
    @OneToOne
    private ExecutorNode node;

    @OneToMany
    private RegisteredJob job;

    private Boolean isEnabled;

    public ExecutorNode getNode() {
        return node;
    }

    public void setNode(ExecutorNode node) {
        this.node = node;
    }

    public RegisteredJob getJob() {
        return job;
    }

    public void setJob(RegisteredJob job) {
        this.job = job;
    }
}