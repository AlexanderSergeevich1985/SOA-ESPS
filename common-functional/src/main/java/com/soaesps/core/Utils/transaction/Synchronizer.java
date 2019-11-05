package com.soaesps.core.Utils.transaction;

import com.soaesps.core.Utils.DataStructure.LRUInMemoryCache;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Synchronizer<ID extends Comparable<ID>> {
    private LRUInMemoryCache<ID, Locker> cache;

    public static class Locker<T extends Number> {
        private ReadWriteLock lock = new ReentrantReadWriteLock();
    }
}