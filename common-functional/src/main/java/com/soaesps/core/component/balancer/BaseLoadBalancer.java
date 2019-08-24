package com.soaesps.core.component.balancer;

import com.soaesps.core.BaseOperation.Statistics.LightMeanCalculator;
import com.soaesps.core.DataModels.executor.ExecutorNode;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class BaseLoadBalancer {
    private ConcurrentSkipListSet<NodeWrapper> nodes = new ConcurrentSkipListSet<>(createComparator());

    public BaseLoadBalancer() {
    }

    public void addExecutorNode(final NodeWrapper executorNode) {
        nodes.add(executorNode);
    }

    public void addExecutorNode(final List<NodeWrapper> executorNodes) {
        nodes.addAll(executorNodes);
    }

    public ConcurrentSkipListSet<NodeWrapper> getNodes() {
        return nodes;
    }

    public void setNodes(final ConcurrentSkipListSet<NodeWrapper> nodes) {
        this.nodes = nodes;
    }

    static public Comparator<NodeWrapper> createComparator() {//Comparator.comparing(ExecutorNode::getStatistic);
        return (o1, o2) -> {
            int value = o1.getExecutorNode().getStatistic().compareTo(o2.getExecutorNode().getStatistic());
            return value != 0 ? value : o1.getExecutorNode().getId().compareTo(o2.getExecutorNode().getId());
        };
    }

    static public class NodeWrapper {
        private ExecutorNode executorNode;

        private LightMeanCalculator lightMeanCalculator;

        public NodeWrapper(final ExecutorNode executorNode, final LightMeanCalculator lightMeanCalculator) {
            this.executorNode = executorNode;
            this.lightMeanCalculator = lightMeanCalculator;
        }

        public ExecutorNode getExecutorNode() {
            return executorNode;
        }

        public void setExecutorNode(final ExecutorNode executorNode) {
            this.executorNode = executorNode;
        }

        public LightMeanCalculator getLightMeanCalculator() {
            return lightMeanCalculator;
        }

        public void setLightMeanCalculator(final LightMeanCalculator lightMeanCalculator) {
            this.lightMeanCalculator = lightMeanCalculator;
        }
    }
}