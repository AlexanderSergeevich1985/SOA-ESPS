package com.soaesps.core.component.aggregator;

import com.soaesps.core.Utils.DateTimeHelper;

import java.time.Duration;

@FunctionalInterface
public interface ReleaseStrategy {
    Long DEFAULT_ELAPSED_INTERVAl = 10000L;

    Integer DEFAULT_MAX_SIZE = 1000;

    boolean isNeedToRelease(final MessageBatch messages);

    class TimeReleaseStrategy implements ReleaseStrategy {
        public boolean isNeedToRelease(final MessageBatch batch) {
            return DateTimeHelper.isElapsed(batch.getTimestamp(), Duration.ofSeconds(DEFAULT_ELAPSED_INTERVAl));
        }
    }

    class SizeReleaseStrategy implements ReleaseStrategy {
        public boolean isNeedToRelease(final MessageBatch batch) {
            final Integer batchSize = batch.getBatchSize();

            return DEFAULT_MAX_SIZE.compareTo(batchSize) >= 0;
        }
    }

    abstract class ChainReleaseStrategy implements ReleaseStrategy {
        private ChainReleaseStrategy next;

        private ReleaseStrategy strategy;

        private ChainReleaseStrategy(final ReleaseStrategy strategy,
                                     final ChainReleaseStrategy next) {
            this.next = next;
            this.strategy = strategy;
        }

        public boolean isNeedToRelease(final MessageBatch batch) {
            return strategy.isNeedToRelease(batch) || next.isNeedToRelease(batch);
        }
    }

    class LimitReleaseStrategy extends ChainReleaseStrategy {
        public LimitReleaseStrategy() {
            super(getSizeReleaseStrategy(), endReleaseStrategy(getTimeReleaseStrategy()));
        }

        public boolean isNeedToRelease(final MessageBatch batch) {
            final Integer batchSize = batch.getBatchSize();

            return DEFAULT_MAX_SIZE.compareTo(batchSize) >= 0;
        }
    }

    static ReleaseStrategy getTimeReleaseStrategy() {
        return new TimeReleaseStrategy();
    }

    static ReleaseStrategy getSizeReleaseStrategy() {
        return new SizeReleaseStrategy();
    }

    static ChainReleaseStrategy endReleaseStrategy(final ReleaseStrategy releaseStrategy) {
        return new ChainReleaseStrategy(releaseStrategy, null) {
            @Override
            public boolean isNeedToRelease(final MessageBatch batch) {
                return super.strategy.isNeedToRelease(batch);
            }
        };
    }

    static ReleaseStrategy getLimitReleaseStrategy() {
        return new LimitReleaseStrategy();
    }
}