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

    private Instant medianTime;

    public NTPService() {}

    public NTPService(final CacheI<String, NodeTimeDesc> cache) {
        this.cache = cache;
    }

    public ZonedDateTime getNodeTimeEval(final String nodeId, final ZoneId zoneId) {
        final NodeTimeDesc timeDesc = cache.get(nodeId);
        if (timeDesc == null) {
            return null;
        }
        return medianTime.minusNanos(timeDesc.getDelay().getNano()).atZone(zoneId);
    }

    public void calcMedianTime() {
        final OptionalDouble avgDiff = Stream.generate(() -> cache.peekFirst())
                .limit(cache.size())
                .filter(Objects::nonNull)
                .mapToLong(NodeTimeDesc::getDiffNodeServerTime)
                .average();
        this.medianTime = Instant.now().plusNanos((long) avgDiff.getAsDouble());
    }

    public CacheI<String, NodeTimeDesc> getCache() {
        return cache;
    }

    public void setCache(final CacheI<String, NodeTimeDesc> cache) {
        this.cache = cache;
    }

    public ZonedDateTime getMedianTime(final ZoneId zoneId) {
        return medianTime.atZone(zoneId);
    }

    public void setMedianTime(final Instant medianTime) {
        this.medianTime = medianTime;
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