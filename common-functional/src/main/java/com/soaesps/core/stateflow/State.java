package com.soaesps.core.stateflow;

import java.util.UUID;

public class State<T> {
    private UUID uuid;

    private String hash; //Previous state arxiv hash

    private T state;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(final UUID uuid) {
        this.uuid = uuid;
    }

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