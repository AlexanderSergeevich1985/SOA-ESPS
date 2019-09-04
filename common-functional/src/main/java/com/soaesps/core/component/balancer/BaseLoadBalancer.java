package com.soaesps.core.component.balancer;

import com.soaesps.core.BaseOperation.Statistics.LightMeanCalculator;
import com.soaesps.core.DataModels.executor.ExecutorNode;
import com.soaesps.core.DataModels.executor.NodeStatistic;
import com.soaesps.core.DataModels.task.BaseJobDesc;
import com.soaesps.core.Utils.DateTimeHelper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class BaseLoadBalancer {
    private ConcurrentHashMap<String, UsedNodeWrapper> usedNodesReg = new ConcurrentHashMap<>();

    private ConcurrentSkipListSet<UsedNodeWrapper> usedNodes = new ConcurrentSkipListSet<>(createComparatorUsed());

    private ConcurrentSkipListSet<NodeWrapper> nodes = new ConcurrentSkipListSet<>(createComparator());

    public BaseLoadBalancer() {
    }

    public NodeWrapper getBestNodeToUse() {
        NodeWrapper nodeWrapper = nodes.pollLast();
        usedNodes.add(new UsedNodeWrapper(nodeWrapper));

        return nodeWrapper;
    }

    public NodeWrapper updateNodeStatistic(final String nodeKey, final String jobKey) {
        UsedNodeWrapper nodeWrapper = usedNodesReg.get(nodeKey);
        JobDescWrapper jobDescWrapper = nodeWrapper.get
        usedNodes.add(new UsedNodeWrapper(nodeWrapper));

        return nodeWrapper;
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

    static public Comparator<UsedNodeWrapper> createComparatorUsed() {//Comparator.comparing(ExecutorNode::getStatistic);
        return (o1, o2) -> {
            int value = o1.getExecutorNode().getStatistic().compareTo(o2.getExecutorNode().getStatistic());
            return value != 0 ? value : o1.getExecutorNode().getId().compareTo(o2.getExecutorNode().getId());
        };
    }

    static public class UsedNodeWrapper extends NodeWrapper {
        private ConcurrentHashMap<String, JobDescWrapper> jobs = new ConcurrentHashMap<>();

        public UsedNodeWrapper(final NodeWrapper nodeWrapper) {
            super(nodeWrapper.getExecutorNode(), nodeWrapper.getLightMeanCalculator());
        }

        public UsedNodeWrapper(final ExecutorNode executorNode, final LightMeanCalculator lightMeanCalculator) {
            super(executorNode, lightMeanCalculator);
        }

        public JobDescWrapper getJobsDesc() {
            return jobs.;
        }

        public void setJobDesc(final JobDescWrapper jobDescWrapper) {
            jobs.put(jobDescWrapper.getJobKey(), jobDescWrapper);
            jobDescWrapper.startJobCount();
        }
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

    static class JobDescWrapper {
        private String jobKey;

        private DateTimeHelper.StopWatch stopWatch = new DateTimeHelper.StopWatch();

        private BaseJobDesc jobDesc;

        public JobDescWrapper(final BaseJobDesc jobDesc) {
            this.jobDesc = jobDesc;
        }

        public String getJobKey() {
            return jobKey;
        }

        public void setJobKey(final String jobKey) {
            this.jobKey = jobKey;
        }

        public void startJobCount() {
            final LocalDateTime dateTime = LocalDateTime.now();
            this.stopWatch.setStart(dateTime.toInstant(ZoneOffset.UTC));
            jobDesc.setStartTime(dateTime);
        }

        public void stopJobCount() {
            final LocalDateTime dateTime = LocalDateTime.now();
            this.stopWatch.setStop(dateTime.toInstant(ZoneOffset.UTC));
            jobDesc.setEndTime(dateTime);
        }

        public BaseJobDesc getJobDesc() {
            return jobDesc;
        }

        public void setJobDesc(final BaseJobDesc jobDesc) {
            this.jobDesc = jobDesc;
        }

        public DateTimeHelper.StopWatch getStopWatch() {
            return stopWatch;
        }

        public void setStopWatch(final DateTimeHelper.StopWatch stopWatch) {
            this.stopWatch = stopWatch;
        }
    }
}