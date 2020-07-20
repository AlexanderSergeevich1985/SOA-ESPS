package com.soaesps.core.component.synchronizer;

import com.soaesps.core.BaseOperation.Statistics.LightDeviationCalculator;
import com.soaesps.core.Utils.DataStructure.QueueI;
import com.soaesps.core.Utils.DateTimeHelper;
import com.soaesps.core.component.router.TransportI;
import com.soaesps.core.stateflow.DataPortionState;
import com.soaesps.core.stateflow.PortionStateTransition;
import com.soaesps.core.stateflow.State;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

abstract public class StateSyncService {
    public static final Long DEFAULT_INITIAL_DELAY = 0l;

    public static final Long DEFAULT_PERIOD = 4l;

    public static Float PROC_LOAD_FACTOR = 0.7f;

    private AtomicLong currentIteration = new AtomicLong(0L);

    private AtomicInteger counter = new AtomicInteger(0);

    private AtomicReference<QueueI<PortionStateTransition>> transitionStatesQueueRef;

    private State dataPortionState;

    private QueueI<PortionStateTransition> transitionQueue;

    private TransportI sender;

    private String url;

    private Long timeToWait;

    private LightDeviationCalculator calculator = new LightDeviationCalculator(0, 0, 10);

    public DateTimeHelper.StopWatch watch = new DateTimeHelper.StopWatch();

    private ScheduledFuture<?> scheduledFuture;

    private Future<Boolean> isUpdated;

    private ExecutorService singleExecutor = Executors.newSingleThreadExecutor();

    private ScheduledExecutorService pool = Executors
            .newScheduledThreadPool(Math.round(PROC_LOAD_FACTOR * Runtime.getRuntime().availableProcessors()));

    public StateSyncService(final TransportI sender, final String url) {
        this.sender = sender;
        this.url = url;
    }

    public void start() {
        this.transitionStatesQueueRef = new AtomicReference<>(transitionQueue);
        scheduledFuture = pool
                .scheduleAtFixedRate(this::stateSyncTask,
                        DEFAULT_INITIAL_DELAY, DEFAULT_PERIOD, TimeUnit.MILLISECONDS);
    }

    public void stateSyncTask() {
        try {
            watch.reset().start();
            if (!isUpdated.isDone() && !isUpdated.isCancelled()) {
            }
            int counter = this.counter.getAndSet(0);
            final Long iteration = this.currentIteration.getAndAdd(1);
            final DataPortionState dataPortionState = new DataPortionState(iteration);
            final Set<PortionStateTransition> transitions = new HashSet<>();
            PortionStateTransition stateTransition = transitionQueue.pull();
            while (counter > 0 || stateTransition != null) {
                transitions.add(stateTransition);
                stateTransition = transitionQueue.pull();
                --counter;
            }
            dataPortionState.setLastGlobalFixedState(this.dataPortionState);
            dataPortionState.setBatchOfUpdates(transitions);
            this.isUpdated = singleExecutor.submit(() -> sendAndSave(dataPortionState));
            calculator.update(watch.stop().getTimeMeasurement(TimeUnit.MILLISECONDS));
        } catch (final Exception ex) {// InterruptedException | ExecutionException

        }
    }

    public boolean sendAndSave(final DataPortionState dataPortionState) {
        final Set<State> newFixedStates = this.sender.sendAndGet(url, dataPortionState);
        if (newFixedStates != null && saveNewFixedState(newFixedStates)) {
            this.dataPortionState = newFixedStates
                    .stream()
                    .filter(s -> s.getUuid().equals(dataPortionState.getUuid()))
                    .findFirst()
                    .orElse(this.dataPortionState);

            return true;
        }

        return false;
    }

    abstract public boolean preCalculateState(final DataPortionState dataPortionState);

    abstract public boolean saveNewFixedState(final Set<State> newFixedStates);

    public void stop() {
        transitionStatesQueueRef.set(null);
        if (pool != null && !pool.isShutdown()
                && !pool.isTerminated()) {
            final long timeToWait = timeToWait();
            if (timeToWait == 0) {
                pool.shutdownNow();
            } else {
                pool.schedule(() -> stop(), timeToWait, TimeUnit.MILLISECONDS);
            }
        }
    }

    protected long timeToWait() {
        return transitionQueue.getSize() * timeToWait;
    }

    public State getDataPortionState() {
        return dataPortionState;
    }

    public void setDataPortionState(final State dataPortionState) {
        this.dataPortionState = dataPortionState;
    }

    public AtomicReference<QueueI<PortionStateTransition>> getTransitionStatesQueueRef() {
        return transitionStatesQueueRef;
    }

    public Long getTimeToWait() {
        return timeToWait;
    }

    public void setTimeToWait(final Long timeToWait) {
        this.timeToWait = timeToWait;
    }

    public TransportI getSender() {
        return sender;
    }

    public void setSender(final TransportI sender) {
        this.sender = sender;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }
}