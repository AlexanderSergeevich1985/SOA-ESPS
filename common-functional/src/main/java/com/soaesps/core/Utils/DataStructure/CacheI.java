package com.soaesps.core.Utils.DataStructure;

import java.util.Comparator;

public interface CacheI<ID extends Comparable<ID>, T> {
    long DEFAULT_MAX_CASHE_SIZE = 1000l;

    T addWithEvict(ID key, T object);

    T get(final ID key);

    T peekLast();

    T peekFirst();

    T updateValue(final ID key, final T object);

    T remove(final ID key);

    int clear();

    long size();

    Comparator<AbstractInMemoryCache.CacheKey<ID>> createComparator();
}