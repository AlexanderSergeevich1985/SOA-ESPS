package com.soaesps.core.DataModels.executor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.soaesps.core.DataModels.device.DeviceDesc;
import com.soaesps.core.DataModels.task.RegisteredJob;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "ref_node_job")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class NodeJobRegister {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeJobRegister that)) return false;
        return Objects.equals(id, that.id)
                && Objects.equals(node, that.node)
                && Objects.equals(job, that.job);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, node, job);
    }
}