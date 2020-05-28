package com.soaesps.core.DataModels.executor;

import com.soaesps.core.DataModels.BaseEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "REF_NODE_STATISTIC")
public class NodeStatistic extends BaseEntity implements Comparable<NodeStatistic> {
    public static final String NODE_ID = "nodeId";

    public static final String PERFORMANCE_INDEX = "performanceIndex";

    public static final String FAILURE_RATE = "failureRate";

    public static final String FAILURE_PROBABILITY = "failureProbability";

    @Column(name = "node_id", nullable = false)
    private Long nodeId;

    @Column(name = "performance_index", nullable = true)
    private Double performanceIndex;

    @Column(name = "failure_rate", nullable = true)
    private Double failureRate;

    @Column(name = "failure_probability", nullable = true, precision = 5, scale = 4)
    private Double failureProbability;

    @Transient
    private Long delay;

    protected NodeStatistic() {}

    @Nonnull
    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(@Nonnull Long nodeId) {
        this.nodeId = nodeId;
    }

    @Nullable
    public Double getPerformanceIndex() {
        return performanceIndex;
    }

    public void setPerformanceIndex(@Nullable Double performanceIndex) {
        this.performanceIndex = performanceIndex;
    }

    @Nullable
    public Double getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(@Nullable Double failureRate) {
        this.failureRate = failureRate;
    }

    @Nullable
    public Double getFailureProbability() {
        return failureProbability;
    }

    public void setFailureProbability(@Nullable Double failureProbability) {
        this.failureProbability = failureProbability;
    }

    @Override
    public int compareTo(final NodeStatistic nodeStatistic) {
        return performanceIndex.compareTo(nodeStatistic.getPerformanceIndex());
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(final Long delay) {
        this.delay = delay;
    }
}