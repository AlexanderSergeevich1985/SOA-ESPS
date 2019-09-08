package com.soaesps.core.component.curcuitBreaker;

import com.soaesps.core.BaseOperation.Statistics.LightDeviationCalculator;
import com.soaesps.core.DataModels.task.BaseJobDesc;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BaseCircuitBreaker {
    static public int DEFAUT_SIZE = 1024;

    static public Double threshold = 1000d;

    private ConcurrentHashMap<String, NodeStatistic> nodeStats = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, RefJobDesc> jobsDescs = new ConcurrentHashMap<>();

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

        final CircuitState state = circuitStates.get(nodeId);
        final JobDesc reference = jobsDescs.get(jobKey);
        if (StatisticComparator.isOutliers(spentTime, reference)) {
            if (state == CircuitState.Half_Open) {
                state.prev();
            }
            else if (state == CircuitState.Closed && StatisticComparator.needToOpen(spentTime, jobDesc, reference)) {
                state.next();
            }
        }
        else {
            if (state == CircuitState.Half_Open && StatisticComparator.canToClosed(spentTime, jobDesc, reference)) {
                state.next();
            }
            double oldValue = jobDesc.getCalculator().getMean();
            jobDesc.updateStats((double) spentTime);
            updateGeneralStats(jobKey, oldValue, jobDesc.getCalculator().getMean());
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

    public void updateGeneralStats(@NotNull final String jobKey, final Double oldValue, final Double newValue) {
        final JobDesc jobDesc = jobsDescs.get(jobKey);
        if (jobDesc == null) {
            return;
        }
        jobDesc.updateStats(oldValue, newValue);
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

    public NodeStatistic removeNode(@NotBlank final String nodeKey) {
        final NodeStatistic nodeStatistic = nodeStats.remove(nodeKey);
        nodeStatistic.getJobs().values().stream().forEach(j -> jobsDescs.get(j.getJobKey()).getCalculator().decSize(j.getCalculator().getMean()));

        return nodeStats.remove(nodeKey);
    }

    public JobDesc removeNodeJob(@NotBlank final String nodeKey, @NotBlank final String jobKey) {
        NodeStatistic nodeStatistic = nodeStats.get(nodeKey);
        if (nodeStatistic == null) {
            return null;
        }
        final JobDesc jobDesc = nodeStatistic.removeOneJob(jobKey);
        jobsDescs.get(jobKey).getCalculator().decSize(jobDesc.getCalculator().getMean());

        return jobDesc;
    }

    public NodeStatistic addNode(@NotBlank final String nodeKey, @NotBlank final NodeStatistic nodeStatistic) {
        nodeStatistic.getJobs().values().stream().forEach(j -> jobsDescs.get(j.getJobKey()).getCalculator().incSize(j.getCalculator().getMean()));

        return nodeStats.put(nodeKey, nodeStatistic);
    }

    public JobDesc addNodeJob(@NotBlank final String nodeKey, @NotBlank final String jobKey, @NotNull final JobDesc jobDesc) {
        NodeStatistic nodeStatistic = nodeStats.get(nodeKey);
        if (nodeStatistic == null) {
            return null;
        }
        nodeStatistic.getJobs().put(jobKey, jobDesc);
        jobsDescs.get(jobKey).getCalculator().incSize(jobDesc.getCalculator().getMean());

        return jobDesc;
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

    static public class RefJobDesc extends JobDesc {
        private String jobKey;

        private BaseJobDesc jobDesc;

        private LightDeviationCalculator calculator;

        private Integer counter;

        public RefJobDesc(final BaseJobDesc jobDesc, final LightDeviationCalculator calculator) {
            super(jobDesc, calculator);
            this.counter = 0;
        }

        public Integer getCounter() {
            return counter;
        }

        public void setCounter(final Integer counter) {
            this.counter = counter;
        }

        public double updateStats(final Double oldValue, final Double newValue) {
            return calculator.update(oldValue, newValue);
        }

        public double regNodeJob(final Double oldValue, final Double newValue) {
            return calculator.update(oldValue, newValue);
        }
    }

    static public class StatisticComparator<T> {
        static public boolean isOutliers(final Long spentTime, final JobDesc reference) {
            return spentTime > threshold;
        }

        static public boolean canToClosed(final Long spentTime, final JobDesc nodeJobDesc, final JobDesc reference) {
            return spentTime > threshold;
        }

        static public boolean needToOpen(final Long spentTime, final JobDesc nodeJobDesc, final JobDesc reference) {
            return spentTime > threshold;
        }
    }
}