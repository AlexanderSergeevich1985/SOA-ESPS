package com.soaesps.core.graph;

import com.google.common.util.concurrent.AtomicDouble;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PathDesc implements PathDescI {
    public AtomicBoolean isChangedAfterAdded;

    private AtomicInteger state = new AtomicInteger(VertexState.UNVISITED.getValue());

    public AtomicDouble cost = new AtomicDouble(Double.MAX_VALUE);

    @Override
    public Double getCost() {
        return cost.doubleValue();
    }

    @Override
    public void setCost(final Double cost) {
        if (this.isChangedAfterAdded != null) {
            this.isChangedAfterAdded.compareAndSet(false, true);
        }
        else {
            this.isChangedAfterAdded = new AtomicBoolean(false);
        }

        final double currentValue = this.cost.get();
        if (currentValue <= cost) {
            return;
        }

        if (!this.cost.compareAndSet(currentValue, cost)) {
            setCost(cost);
        }
    }

    @Override
    public VertexState getState() {
        return VertexState.builder(state.get());
    }

    @Override
    public void setState(final VertexState state) {
        this.state.set(state.getValue());
    }

    @Override
    public boolean isChangedAfterAdded() {
        return this.isChangedAfterAdded.get();
    }
}