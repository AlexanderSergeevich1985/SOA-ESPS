package com.soaesps.core.component.storage;

import com.soaesps.core.Utils.DataStructure.ObjectConverter;

import java.util.Comparator;
import java.util.Objects;

/**
 * Created by sniper on 19.11.22.
 */
public class Range<T> implements RangeI<T> {
    private Comparator<T> comparator;

    protected Integer interval;

    protected T start;

    protected T end;

    public Range(T start, T end, Integer interval, Comparator<T> comparator) {
        this.start = start;
        this.end = end;
        this.interval = interval;
        this.comparator = comparator;
    }

    public boolean valueInRange(T value, Boolean startIn, Boolean endIn) {
        int sc = comparator.compare(start, value);
        int ec = comparator.compare(end, value);

        return sc > 0 && ec < 0 || startIn && sc == 0 || endIn && ec == 0;
    }

    public <T1 extends Number> long getIndexByValue(T1 value) {
        return (long) value/interval;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof RangeI)) {
            return false;
        }
        if (this == other) {
            return true;
        }
        Range<T> instance = ObjectConverter.cast(other, this.getClass());

        return Objects.equals(this.start, instance.start) && Objects.equals(this.end, instance.end);
    }
}