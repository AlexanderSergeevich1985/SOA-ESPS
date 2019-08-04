/**The MIT License (MIT)
 Copyright (c) 2019 by AleksanderSergeevich
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package com.soaesps.core.Utils.DataStructure;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractInMemoryCache<ID extends Comparable<ID>, T> implements CacheI<ID, T> {
    protected ConcurrentHashMap<ID, ObjWraper<ID, T>> objects;

    protected ConcurrentSkipListSet<CacheKey<ID>> keys;

    public AbstractInMemoryCache() {
        objects = new ConcurrentHashMap<>();
        keys = new ConcurrentSkipListSet<>(createComparator());
    }

    public AbstractInMemoryCache(final Comparator<CacheKey<ID>> comparator) {
        objects = new ConcurrentHashMap<>();
        keys = new ConcurrentSkipListSet<>(comparator);
    }

    public void setComparator(final Comparator<CacheKey<ID>> comparator) {
        ConcurrentSkipListSet<CacheKey<ID>> newKeys = new ConcurrentSkipListSet<>(comparator);
        newKeys.addAll(keys);
        keys = newKeys;
    }

    @Override
    public T addWithEvict(final ID key, final T object) {
        ObjWraper<ID, T> worstObject = null;
        if (objects.size() >= DEFAULT_MAX_CASHE_SIZE) {
            CacheKey<ID> worstKey = keys.pollFirst();
            worstObject = objects.remove(worstKey.getKey());
        }
        CacheKey<ID> cacheKey = new CacheKey<>(key);
        cacheKey.setLastUpdate(LocalDateTime.now());
        cacheKey.setFrequency(1l);
        keys.add(cacheKey);
        objects.put(key, new ObjWraper<>(cacheKey, object));

        return worstObject != null ? worstObject.getItem() : null;
    }

    @Override
    public T get(final ID key) {
        ObjWraper<ID, T> wraper = objects.get(key);
        if (wraper == null) {
            return null;
        }
        CacheKey<ID> cacheKey = wraper.getCacheKey();
        keys.remove(cacheKey);
        updateStats(cacheKey);
        keys.add(cacheKey);

        return wraper.getItem();
    }

    abstract protected void updateStats(final CacheKey<ID> key);

    @Override
    public T updateValue(final ID key, final T object) {
        ObjWraper<ID, T> wraper = objects.get(key);
        if (wraper == null) {
            return null;
        }
        T oldObject = wraper.getItem();
        wraper.setItem(object);
        CacheKey<ID> cacheKey = wraper.getCacheKey();
        keys.remove(cacheKey);
        updateStats(cacheKey);
        keys.add(cacheKey);

        return oldObject;
    }

    @Override
    public T remove(final ID key) {
        ObjWraper<ID, T> deleted = objects.remove(key);
        if (deleted == null) {
            return null;
        }
        keys.remove(deleted.getCacheKey());

        return deleted.getItem();
    }

    @Override
    public int clear() {
        int size = objects.size();
        objects.clear();

        return size;
    }

    @Override
    public long size() {
        return this.objects.size();
    }

    @Override
    public T peekFirst() {
        ObjWraper<ID, T> objWraper = objects.get(keys.first().getKey());

        return objWraper != null ? objWraper.getItem() : null;
    }

    @Override
    public T peekLast() {
        ObjWraper<ID, T> objWraper = objects.get(keys.last().getKey());

        return objWraper != null ? objWraper.getItem() : null;
    }

    abstract public Comparator<CacheKey<ID>> createComparator();

    static public class CacheKey<ID> {
        private LocalDateTime lastUpdate;

        private Long frequency;

        private ID key;

        public CacheKey(final ID key) {
            this.key = key;
        }

        public LocalDateTime getLastUpdate() {
            return lastUpdate;
        }

        public void setLastUpdate(final LocalDateTime lastUpdate) {
            this.lastUpdate = lastUpdate;
        }

        public Long getFrequency() {
            return frequency;
        }

        public void setFrequency(final Long frequency) {
            this.frequency = frequency;
        }

        public ID getKey() {
            return key;
        }

        public void setKey(final ID key) {
            this.key = key;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(final Object object) {
            if (object == null || !(object instanceof CacheKey)) {
                return false;
            }
            if (this == object) {
                return true;
            }
            CacheKey cacheKey = (CacheKey) object;
            if (key == null || cacheKey.getKey() == null) {
                return false;
            }

            return key.equals(cacheKey.getKey());
        }
    }

    public static class ObjWraper<ID extends Comparable<ID>, T> {
        private CacheKey<ID> cacheKey;

        private T item;

        public ReadWriteLock rwLock = new ReentrantReadWriteLock();

        private AtomicInteger counter = new AtomicInteger(1);

        public ObjWraper(final CacheKey<ID> cacheKey, final T item) {
            this.cacheKey = cacheKey;
            this.item = item;
        }

        public CacheKey<ID> getCacheKey() {
            return cacheKey;
        }

        public void setCacheKey(final CacheKey<ID> cacheKey) {
            this.cacheKey = cacheKey;
        }

        public final <T> T readItem() {
            return (T) (rwLock.readLock().tryLock() ? item : null);
        }

        public <T> T writeItem() {
            return (T) (rwLock.writeLock().tryLock() ? item : null);
        }

        public T getItem() {
            return item;
        }

        public void setItem(final T item) {
            this.item = item;
        }

        @Override
        public int hashCode() {
            return item.hashCode();
        }

        @Override
        public boolean equals(final Object object) {
            if (object == null || !(object instanceof ObjWraper)) {
                return false;
            }
            ObjWraper wraper = (ObjWraper) object;
            if (item == null || wraper.getItem() == null) {
                return false;
            }

            return item.equals(wraper.getItem());
        }
    }
}