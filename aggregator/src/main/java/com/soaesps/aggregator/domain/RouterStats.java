package com.soaesps.aggregator.domain;

import com.soaesps.aggregator.client.IWorkerNode;
import com.soaesps.core.DataModels.BaseEntity;

import javax.persistence.Entity;

@Entity
public class RouterStats extends BaseEntity {
    private double score;

    private IWorkerNode workerNode;

    public RouterStats() {}

    public double getCounter() {
        return score;
    }

    public void setCounter(final double score) {
        this.score = score;
    }

    public IWorkerNode getWorkerNode() {
        return workerNode;
    }

    public void setWorkerNode(final IWorkerNode workerNode) {
        this.workerNode = workerNode;
    }
}
