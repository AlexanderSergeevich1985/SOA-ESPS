package com.soaesps.core.component.balancer;

import com.soaesps.core.BaseOperation.Statistics.LightDeviationCalculator;
import com.soaesps.core.DataModels.executor.ExecutorNode;
import com.soaesps.core.DataModels.task.BaseJobDesc;
import com.soaesps.core.Utils.DateTimeHelper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

public class BaseLoadBalancer {
    private ConcurrentHashMap<String, UsedNodeWrapper> usedNodesReg = new ConcurrentHashMap<>();

    private ConcurrentSkipListSet<UsedNodeWrapper> usedNodes = new ConcurrentSkipListSet<>(createComparatorUsed());

    private ConcurrentSkipListSet<NodeWrapper> nodes = new ConcurrentSkipListSet<>(createComparator());

    private ConcurrentHashMap<String, JobDesc> refJobsDescs = new ConcurrentHashMap<>();

    public BaseLoadBalancer() {
    }

    public ConcurrentHashMap<String, JobDesc> getRefJobsDescs() {
        return refJobsDescs;
    }

    public void setRefJobsDescs(final ConcurrentHashMap<String, JobDesc> refJobsDescs) {
        this.refJobsDescs = refJobsDescs;
    }

    public void addRefJobDesc(final String jobKey, final JobDesc jobDesc) {
        refJobsDescs.put(jobKey, jobDesc);
    }

    public JobDesc getRefJobDesc(final String jobKey) {
        return refJobsDescs.get(jobKey);
    }

    public NodeWrapper getBestNodeToUse() {
        NodeWrapper nodeWrapper = nodes.pollLast();
        usedNodes.add(new UsedNodeWrapper(nodeWrapper));

        return nodeWrapper;
    }

    public NodeWrapper updateNodeStatistic(final String nodeKey, final String jobKey, final Long newValue) {
        UsedNodeWrapper nodeWrapper = usedNodesReg.get(nodeKey);
        JobDesc refJobDesc = refJobsDescs.get(jobKey);
        if (refJobDesc == null || refJobDesc.getCalculator() == null) {
            return null;
        }
        double refMean = refJobDesc.getCalculator().getMean();
        nodeWrapper.getLightDeviationCalculator().update(newValue/refMean);

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
        private ConcurrentHashMap<String, UsedJobDesc> usedJobs = new ConcurrentHashMap<>();

        public UsedNodeWrapper(final NodeWrapper nodeWrapper) {
            super(nodeWrapper.getExecutorNode(), nodeWrapper.getLightDeviationCalculator());
        }

        public UsedNodeWrapper(final ExecutorNode executorNode, final LightDeviationCalculator lightDeviationCalculator) {
            super(executorNode, lightDeviationCalculator);
        }

        public UsedJobDesc getJobsDesc(final String jobKey) {
            return usedJobs.get(jobKey);
        }

        public void setJobDesc(final UsedJobDesc usedJobDesc) {
            usedJobs.put(usedJobDesc.getJobKey(), usedJobDesc);
        }
    }

    static public class NodeWrapper {
        private ExecutorNode executorNode;

        private LightDeviationCalculator lightDeviationCalculator;

        private ConcurrentHashMap<String, JobDesc> jobs = new ConcurrentHashMap<>();

        public NodeWrapper(final ExecutorNode executorNode, final LightDeviationCalculator lightDeviationCalculator) {
            this.executorNode = executorNode;
            this.lightDeviationCalculator = lightDeviationCalculator;
        }

        public ExecutorNode getExecutorNode() {
            return executorNode;
        }

        public void setExecutorNode(final ExecutorNode executorNode) {
            this.executorNode = executorNode;
        }

        public LightDeviationCalculator getLightDeviationCalculator() {
            return lightDeviationCalculator;
        }

        public void setLightDeviationCalculator(final LightDeviationCalculator lightDeviationCalculator) {
            this.lightDeviationCalculator = lightDeviationCalculator;
        }

        public ConcurrentHashMap<String, JobDesc> getJobsDescs() {
            return jobs;
        }

        public void setJobsDescs(final ConcurrentHashMap<String, JobDesc> jobs) {
            this.jobs = jobs;
        }
    }

    static public class UsedJobDesc extends JobDesc {
        private Map<String, DateTimeHelper.StopWatch> stopWatchs = new HashMap<>();

        public UsedJobDesc(final BaseJobDesc jobDesc, final LightDeviationCalculator calculator) {
            super(jobDesc, calculator);
        }

        public void startJobCount(final String jobId) {
            final LocalDateTime dateTime = LocalDateTime.now();
            DateTimeHelper.StopWatch stopWatch = stopWatchs.get(jobId);
            stopWatch.setStart(dateTime.toInstant(ZoneOffset.UTC));
        }

        public void stopJobCount(final String jobId) {
            final LocalDateTime dateTime = LocalDateTime.now();
            DateTimeHelper.StopWatch stopWatch = stopWatchs.get(jobId);
            stopWatch.setStart(dateTime.toInstant(ZoneOffset.UTC));
            updateStats(stopWatch.getTimeMeasurement(TimeUnit.MILLISECONDS).doubleValue());
        }

        public DateTimeHelper.StopWatch getStopWatch(final String jobId) {
            return stopWatchs.get(jobId);
        }

        public void setStopWatch(final String jobId, final DateTimeHelper.StopWatch stopWatch) {
            this.stopWatchs.put(jobId, stopWatch);
        }
    }

    static public class JobDesc {
        private String jobKey;

        private BaseJobDesc jobDesc;

        private LightDeviationCalculator calculator;

        public JobDesc(final BaseJobDesc jobDesc, final LightDeviationCalculator calculator) {
            this.jobDesc = jobDesc;
            this.calculator = calculator;
        }

        public double updateStats(final Double newValue) {
            return calculator.update(newValue);
        }

        public double updateStats(final Double oldValue, final Double newValue) {
            return calculator.update(oldValue, newValue);
        }

        public String getJobKey() {
            return jobKey;
        }

        public void setJobKey(final String jobKey) {
            this.jobKey = jobKey;
        }

        public BaseJobDesc getJobDesc() {
            return jobDesc;
        }

        public void setJobDesc(final BaseJobDesc jobDesc) {
            this.jobDesc = jobDesc;
        }

        public LightDeviationCalculator getCalculator() {
            return calculator;
        }

        public void setCalculator(final LightDeviationCalculator calculator) {
            this.calculator = calculator;
        }
    }
}