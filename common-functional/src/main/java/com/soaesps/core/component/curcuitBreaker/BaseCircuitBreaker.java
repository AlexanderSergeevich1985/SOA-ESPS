package com.soaesps.core.component.curcuitBreaker;

import com.soaesps.core.BaseOperation.Statistics.LightDeviationCalculator;
import com.soaesps.core.DataModels.task.BaseJobDesc;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BaseCircuitBreaker {
    static public int DEFAUT_SIZE = 1024;

    static public Double threshold = 1000d;

    private ConcurrentHashMap<String, NodeStatistic> nodeStats = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, JobDesc> jobsDescs = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, CircuitState> circuitStates = new ConcurrentHashMap<>();

    public BaseCircuitBreaker() {
    }

    public CircuitState updateStatistic(final String nodeId, final String jobKey, final long spentTime) {
        final NodeStatistic node = nodeStats.get(nodeId);
        if (node == null) {
            return null;
        }
        final JobDesc jobDesc = node.jobs.get(jobKey);
        if (jobDesc == null) {
            return null;
        }
        double oldValue = jobDesc.updateStats((double) spentTime);
        this.jobsDescs.get(jobKey).updateStats(oldValue, (double) spentTime);
        final CircuitState state = circuitStates.get(nodeId);
        if (jobDesc.getCalculator().getMean() > threshold) {
            state.next();
        }

        return state;
    }

    public void registerNodeJob(@NotNull final String nodeKey, @NotNull final String jobKey, final double devInitValue, final double meanInitValue) {
        NodeStatistic nodeStatistic = nodeStats.get(nodeKey);
        if (nodeStatistic == null) {
            nodeStatistic = new NodeStatistic();
        }
        final LightDeviationCalculator calculator = new LightDeviationCalculator(devInitValue, meanInitValue, DEFAUT_SIZE);
        final BaseJobDesc baseJobDesc = new BaseJobDesc();
        baseJobDesc.setJobKey(jobKey);
        final JobDesc jobDesc = new JobDesc(baseJobDesc, calculator);
        nodeStatistic.addOneJob(jobKey, jobDesc);
        nodeStats.put(jobKey, nodeStatistic);
    }

    public JobDesc removeNodeJob(@NotNull final String nodeKey, @NotNull final String jobKey) {
        NodeStatistic nodeStatistic = nodeStats.get(nodeKey);
        if (nodeStatistic == null) {
            return null;
        }

        return nodeStatistic.removeOneJob(jobKey);
    }

    public void updateGeneralStats(@NotNull final String jobKey, final Double oldValue, final Double newValue) {
        final JobDesc jobDesc = jobsDescs.get(jobKey);
        if (jobDesc == null) {
            return;
        }
        jobDesc
    }

    public void updateStatistic(final String jobKey, final Double oldValue, final Double newValue) {
        nodeStats.values().stream().flatMap(ns -> ns.getJobs().values().stream()).collect(Collectors.groupingBy(JobDesc::getJobKey, Collectors.averagingDouble(jd -> jd.getCalculator().getMean())));
    }

    public CircuitState getCircuitState(final String nodeId) {
        return circuitStates.get(nodeId);
    }

    public void setCircuitState(final String nodeId, final CircuitState circuitState) {
        this.circuitStates.get(nodeId).setState(circuitState);
    }

    static public class NodeStatistic {
        private Map<String, JobDesc> jobs = new ConcurrentHashMap<>();

        public Map<String, JobDesc> getJobs() {
            return jobs;
        }

        public void setJobs(final Map<String, JobDesc> jobs) {
            this.jobs = jobs;
        }

        public JobDesc getOneJob(final String jobKey) {
            return jobs.get(jobKey);
        }

        public void addOneJob(@NotNull final String jobKey, @NotNull final JobDesc jobDesc) {
            jobs.put(jobKey, jobDesc);
        }

        public JobDesc removeOneJob(final String jobKey) {
            return jobs.remove(jobKey);
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
            return calculator.update(newValue);
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