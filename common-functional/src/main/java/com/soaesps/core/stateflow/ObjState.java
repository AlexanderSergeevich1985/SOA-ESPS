package com.soaesps.core.stateflow;

abstract public class ObjState<T, T2> {
    private ObjState<T, T2> prevStateRef;

    private T state;

    private T2 stateDiff;

    public ObjState(ObjState<T, T2> prevStateRef, T2 stateDiff) {
        this.prevStateRef = prevStateRef;
        this.stateDiff = stateDiff;
    }

    public T getUpdatedState() {
        if (isNeedToRecalculate()) {
            this.state = prevStateRef.getUpdatedState();
        }

        return calcUpdatedState(state, stateDiff);
    }

    protected boolean isNeedToRecalculate() {
        return state == null;
    }

    abstract protected T calcUpdatedState(T state, T2 stateDiff);

    public ObjState<T, T2> getPrevStateRef() {
        return prevStateRef;
    }

    public void resetState(ObjState<T, T2> prevStateRef) {
        this.prevStateRef = prevStateRef;
        this.state = null;
    }

    public T getState() {
        return state;
    }

    public void setState(T state) {
        this.state = state;
    }

    public T2 getStateDiff() {
        return stateDiff;
    }

    public void setStateDiff(T2 stateDiff) {
        this.stateDiff = stateDiff;
    }
}