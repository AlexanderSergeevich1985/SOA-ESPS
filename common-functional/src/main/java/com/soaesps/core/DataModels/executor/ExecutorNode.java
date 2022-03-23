package com.soaesps.core.DataModels.executor;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "executor_node")
public class ExecutorNode {
    @Id
    @GenericGenerator(name = "kaugen", strategy = "increment")
    @GeneratedValue(generator = "kaugen")
    @Column(name = "id", nullable = false)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "executorNode")
    private NodeStatistic statistic;

    @Embedded
    private ExecutorNodeDesc nodeDesc;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public ExecutorNodeDesc getNodeDesc() {
        return nodeDesc;
    }

    public void setNodeDesc(final ExecutorNodeDesc nodeDesc) {
        this.nodeDesc = nodeDesc;
    }

    public NodeStatistic getStatistic() {
        return statistic;
    }

    public void setStatistic(final NodeStatistic statistic) {
        this.statistic = statistic;
    }
}