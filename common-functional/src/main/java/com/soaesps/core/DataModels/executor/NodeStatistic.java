package com.soaesps.core.DataModels.executor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.soaesps.core.Utils.convertor.hibernate.TimestampConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "node_statistic")
public class NodeStatistic implements Comparable<NodeStatistic> {
    public static final String NODE_ID = "nodeId";

    public static final String PERFORMANCE_INDEX = "performanceIndex";

    public static final String FAILURE_RATE = "failureRate";

    public static final String FAILURE_PROBABILITY = "failureProbability";

    @Id
    @Column(name = "node_id", nullable = false)
    private Long nodeId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "node_id")
    private ExecutorNode executorNode;

    @Column(name = "creation_time", nullable = false, updatable = false)
    @Convert(converter = TimestampConverter.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ")
    private ZonedDateTime creationTime;

    @JsonIgnore
    @Column(name = "modification_time")
    @Convert(converter = TimestampConverter.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ")
    private ZonedDateTime modificationTime;

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