package com.soaesps.aggregator.domain;

import com.soaesps.aggregator.client.IWorkerNode;

public class RouterStats {
    private double score;

    private IWorkerNode workerNode;

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
