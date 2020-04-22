package com.soaesps.core.component.aggregator;

import java.util.function.Predicate;

public class CorrelationKey {
    private String correlationId;

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(final String correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public int hashCode() {
        return correlationId.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || isNotInstanceOfTheClass.test(other)) {
            return false;
        }
        final CorrelationKey key = (CorrelationKey) other;

        return this.correlationId.equals(key.correlationId);
    }

    private static final Predicate<Object> isInstanceOfTheClass =
            objectToTest -> objectToTest instanceof CorrelationKey;

    private static final Predicate<Object> isNotInstanceOfTheClass =
            isInstanceOfTheClass.negate();
}