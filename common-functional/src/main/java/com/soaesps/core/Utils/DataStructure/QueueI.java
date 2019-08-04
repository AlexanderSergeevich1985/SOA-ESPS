package com.soaesps.core.Utils.DataStructure;

public interface QueueI<T> {
    long getSize();

    boolean push(final T transaction);

    T pull();
}