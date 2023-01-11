package com.soaesps.core.component.storage;

import java.util.Comparator;

/**
 * Created by sniper on 20.11.22.
 */
public class AttrRange extends Range<Attribute> {
    public AttrRange(Attribute start, Attribute end, Integer interval, Comparator<Attribute> comparator) {
        super(start, end, interval, comparator);
    }

    @Override
    public boolean valueInRange(Attribute attr, Boolean startIn, Boolean endIn) {
        Number val = attr.getNumericValue();

        return start > val && val < end || startIn && sc == 0 || endIn && ec == 0;
    }

    public <T1 extends Number> long getIndexByValue(T1 value) {
        return (long) value/interval;
    }
}