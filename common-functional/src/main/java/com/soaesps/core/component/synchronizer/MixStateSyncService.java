package com.soaesps.core.component.synchronizer;

import com.soaesps.core.DataModels.executor.NodeStatistic;
import com.soaesps.core.Utils.DataStructure.LimitedQueue;
import com.soaesps.core.Utils.DataStructure.QueueI;
import com.soaesps.core.stateflow.DataPortionState;
import com.soaesps.core.stateflow.State;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

abstract public class MixStateSyncService<NodeKey extends Serializable> {
    public static Float PROC_LOAD_FACTOR = 0.7f;

    public static final Long DEFAULT_INITIAL_DELAY = 0l;

    public static final Long DEFAULT_PERIOD = 4l;

    private AtomicLong currentIteration = new AtomicLong(0L);

    private QueueI<SyncIteration<NodeKey>> iterationQueue;

    private ConcurrentSkipListMap<NodeStatistic, NodeKey> nodesStatistic;

    private ScheduledFuture<?> future;

    private ScheduledExecutorService pool = Executors
            .newScheduledThreadPool(Math.round(PROC_LOAD_FACTOR * Runtime.getRuntime().availableProcessors()));

    public Long whenApplyDelay;

    public MixStateSyncService() {
        this.iterationQueue = new LimitedQueue<>();
        this.nodesStatistic = new ConcurrentSkipListMap<>();
    }

    public void start() {
        future = pool
                .scheduleAtFixedRate(this::stateSyncTask,
                        DEFAULT_INITIAL_DELAY, DEFAULT_PERIOD, TimeUnit.MILLISECONDS);
    }

    public void stateSyncTask() {
        final Long iteration = this.currentIteration.addAndGet(1);
        final SyncIteration<NodeKey> syncIteration = iterationQueue.pull();
        final Set<State> fixedStates = mixStatesForIteration(iteration, syncIteration.getStatesForIteration());
    }

    abstract public Set<State> mixStatesForIteration(final Long iteration, final Map<NodeKey, Set<DataPortionState>> statesForIteration);

    public void sendFixedStatesAllNodes(final Set<State> fixedStates) {
        Map.Entry<NodeStatistic, NodeKey> entry = nodesStatistic.pollFirstEntry();
        while (entry  != null) {
            sendFixedStatesOneNode(entry.getValue(),
                    whenApplyDelay - entry.getKey().getDelay(), fixedStates);
        }
    }

    abstract public void sendFixedStatesOneNode(final NodeKey nodeKey, final Long whenApplyDelay, final Set<State> fixedStates);

    public QueueI<SyncIteration<NodeKey>> getIterationQueue() {
        return iterationQueue;
    }

    public void setIterationQueue(final QueueI<SyncIteration<NodeKey>> iterationQueue) {
        this.iterationQueue = iterationQueue;
    }

    public Map<NodeStatistic, NodeKey> getNodesStatistic() {
        return nodesStatistic;
    }

    public void setNodesStatistic(final ConcurrentSkipListMap<NodeStatistic, NodeKey> nodesStatistic) {
        this.nodesStatistic = nodesStatistic;
    }

    public void addNodeStatistic(final NodeKey nodeKey, final NodeStatistic nodesStatistic) {
        this.nodesStatistic.put(nodesStatistic, nodeKey);
    }

    public NodeStatistic getNodeStatistic(final NodeKey nodeKey) {
        final Optional<Map.Entry<NodeStatistic, NodeKey>> nodeStatistic = nodesStatistic.entrySet()
                .stream()
                .filter(e -> e.getValue().equals(nodeKey))
                .findFirst();
        return nodeStatistic.isPresent() ? nodeStatistic.get().getKey() : null;
    }

    public static class SyncIteration<NodeKey extends Serializable> implements Serializable {
        private final Long iteration;

        private Map<NodeKey, Set<DataPortionState>> statesForIteration;

        public SyncIteration(final Long iteration) {
            this.iteration = iteration;
            this.statesForIteration = new ConcurrentHashMap<>();
        }

        public Long getIteration() {
            return iteration;
        }

        public void addState(final NodeKey nodeKey, final DataPortionState state) {
            Set<DataPortionState> states = this.statesForIteration.get(nodeKey);
            if (states == null) {
                states = new HashSet<>();
                addStates(nodeKey, states);
            }
            states.add(state);
        }

        public void addStates(final NodeKey nodeKey, final Set<DataPortionState> states) {
            this.statesForIteration.put(nodeKey, states);
        }

        public Map<NodeKey, Set<DataPortionState>> getStatesForIteration() {
            return statesForIteration;
        }

        public void setStatesForIteration(final Map<NodeKey, Set<DataPortionState>> statesForIteration) {
            this.statesForIteration = statesForIteration;
        }
    }
}