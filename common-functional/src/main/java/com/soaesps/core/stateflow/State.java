package com.soaesps.core.stateflow;

public class State<T> {
    private String hash; //Previous state arxiv hash

    private T state;

    public String getHash() {
        return hash;
    }

    public void setHash(final String hash) {
        this.hash = hash;
    }

    public T getState() {
        return state;
    }

    public void setState(final T state) {
        this.state = state;
    }
}