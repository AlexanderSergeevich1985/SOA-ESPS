package com.soaesps.core.Utils.DataStructure;

import java.io.Serializable;
import java.util.Comparator;

public interface CacheI<ID extends Serializable, T> {
    T addWithEvict(ID key, T object);

    T get(final ID key);

    T updateValue(final ID key, final T object);

    T remove(final ID key);

    int clear();

    long size();

    Comparator<AbstractInMemoryCache.CacheKey<ID>> createComparator();
}