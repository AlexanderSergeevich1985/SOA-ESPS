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
        if (attr == null) {
            return false;
        }

        int compStart = this.comparator.compare(attr, this.start);
        int compEnd = this.comparator.compare(attr, this.end);

        // Inside the range (strictly between start and end)
        if (compStart > 0 && compEnd < 0) {
            return true;
        }
        // Exactly at start and start is inclusive
        if (Boolean.TRUE.equals(startIn) && compStart == 0) {
            return true;
        }
        // Exactly at end and end is inclusive
        if (Boolean.TRUE.equals(endIn) && compEnd == 0) {
            return true;
        }

        return false;
    }

    @Override
    public <T1 extends Number> long getIndexByValue(T1 value) {
        if (value == null || this.interval == 0) {
            return 0;
        }
        return value.longValue() / this.interval;
    }

}