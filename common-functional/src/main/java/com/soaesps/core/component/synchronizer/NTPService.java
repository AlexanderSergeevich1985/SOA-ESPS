package com.soaesps.core.component.synchronizer;

import com.soaesps.core.Utils.DataStructure.CacheI;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.stream.Stream;

public class NTPService {
    private CacheI<String, NodeTimeDesc> cache;

    private Long medianDiffTime;

    public NTPService() {}

    public NTPService(final CacheI<String, NodeTimeDesc> cache) {
        this.cache = cache;
    }

    public ZonedDateTime getNodeTimeEval(final String nodeId, final ZoneId zoneId) {
        final NodeTimeDesc timeDesc = cache.get(nodeId);
        if (timeDesc == null) {
            return null;
        }

        return getAvgTime().plusNanos(timeDesc.getDelay().getNano()).atZone(zoneId);
    }

    public Long getNodeDiffTimeEval(final String nodeId) {
        final NodeTimeDesc timeDesc = cache.get(nodeId);
        if (timeDesc == null) {
            return null;
        }

        return timeDesc.getDiffNodeServerTime();
    }

    public void calcMedianTime() {
        final OptionalDouble avgDiff = Stream.generate(() -> cache.peekFirst())
                .limit(cache.size())
                .filter(Objects::nonNull)
                .mapToLong(NodeTimeDesc::getDiffNodeServerTime)
                .average();
        this.medianDiffTime = (long) avgDiff.getAsDouble();
    }

    public Instant getAvgTime() {
        return Instant.now().plusNanos(medianDiffTime);
    }

    public Long getMedianDiffTime() {
        return medianDiffTime;
    }

    public void setMedianDiffTime(final Long medianDiffTime) {
        this.medianDiffTime = medianDiffTime;
    }

    public CacheI<String, NodeTimeDesc> getCache() {
        return cache;
    }

    public void setCache(final CacheI<String, NodeTimeDesc> cache) {
        this.cache = cache;
    }

    public boolean updateTime(final String nodeId, final Long diffTime) {
        final NodeTimeDesc timeDesc = cache.get(nodeId);
        if (timeDesc == null) {
            return false;
        }
        timeDesc.setDiffNodeServerTime(diffTime);

        return true;
    }

    public boolean updateDelay(final String nodeId, final Instant delay) {
        final NodeTimeDesc timeDesc = cache.get(nodeId);
        if (timeDesc == null) {
            return false;
        }
        timeDesc.setDelay(delay);

        return true;
    }

    public boolean updateTimeDelay(final String nodeId, final Long diffTime, final Instant delay) {
        final NodeTimeDesc timeDesc = cache.get(nodeId);
        if (timeDesc == null) {
            return false;
        }
        timeDesc.setDiffNodeServerTime(diffTime);
        timeDesc.setDelay(delay);

        return true;
    }
}