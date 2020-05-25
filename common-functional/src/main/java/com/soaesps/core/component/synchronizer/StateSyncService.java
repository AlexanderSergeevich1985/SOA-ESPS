package com.soaesps.core.component.synchronizer;

import com.soaesps.core.Utils.DataStructure.QueueI;
import com.soaesps.core.component.router.TransportI;
import com.soaesps.core.stateflow.DataPortionState;
import com.soaesps.core.stateflow.PortionStateTransition;
import com.soaesps.core.stateflow.State;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

abstract public class StateSyncService {
    public static final Long DEFAULT_INITIAL_DELAY = 0l;

    public static final Long DEFAULT_PERIOD = 4l;

    public static Float PROC_LOAD_FACTOR = 0.7f;

    private AtomicReference<QueueI<PortionStateTransition>> transitionStatesQueueRef;

    private DataPortionState dataPortionState;

    private QueueI<PortionStateTransition> transitionQueue;

    private TransportI sender;

    private String url;

    private Long timeToWait;

    private ScheduledFuture<?> future;

    private ScheduledExecutorService pool = Executors
            .newScheduledThreadPool(Math.round(PROC_LOAD_FACTOR * Runtime.getRuntime().availableProcessors()));

    public StateSyncService(final TransportI sender, final String url) {
        this.sender = sender;
        this.url = url;
    }

    public void start() {
        future = pool
                .scheduleAtFixedRate(this::stateSyncTask,
                        DEFAULT_INITIAL_DELAY, DEFAULT_PERIOD, TimeUnit.MILLISECONDS);
        this.dataPortionState = new DataPortionState();
        this.transitionStatesQueueRef = new AtomicReference<>(transitionQueue);
    }

    public void stateSyncTask() {
        final Set<PortionStateTransition> transitions = new HashSet<>();
        PortionStateTransition stateTransition = transitionQueue.pull();
        while (stateTransition != null) {
            transitions.add(stateTransition);
            stateTransition = transitionQueue.pull();
        }
        dataPortionState.setBatchOfUpdates(transitions);
        final State newFixedState = this.sender.send(url, dataPortionState);
        if (newFixedState != null && saveNewFixedState(newFixedState)) {
            dataPortionState.setLastGlobalFixedState(newFixedState);
        }
    }

    abstract public boolean saveNewFixedState(final State newFixedState);

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