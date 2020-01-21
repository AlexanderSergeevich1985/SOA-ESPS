package com.soaesps.core.component.synchronizer;

import java.time.Instant;

public class NodeTimeDesc {
    private Long diffNodeServerTime;

    private Instant delay;

    public Long getDiffNodeServerTime() {
        return diffNodeServerTime;
    }

    public void setDiffNodeServerTime(Long diffNodeServerTime) {
        this.diffNodeServerTime = diffNodeServerTime;
    }

    public Instant getDelay() {
        return delay;
    }

    public void setDelay(final Instant delay) {
        this.delay = delay;
    }
}