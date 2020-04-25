package com.soaesps.core.component.aggregator;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

public class MessageBatch<ID extends Serializable> {
    private LocalDateTime timestamp;

    private Integer batchSize;

    protected ConcurrentSkipListSet<ID> keys;

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(final Integer batchSize) {
        this.batchSize = batchSize;
    }

    public ConcurrentSkipListSet<ID> getKeys() {
        return keys;
    }

    public void setKeys(final Collection<ID> keys) {
        this.keys.addAll(keys);
    }

    public void addKey(final ID key) {
        this.keys.add(key);
    }

    public void removeKey(final ID key) {
        this.keys.remove(key);
    }

    public boolean isContainsKey(final ID key) {
        return this.keys.contains(key);
    }
}