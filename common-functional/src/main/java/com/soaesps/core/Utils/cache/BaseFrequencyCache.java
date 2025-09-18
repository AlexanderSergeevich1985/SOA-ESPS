package com.soaesps.core.Utils.cache;

import java.util.HashMap;
import java.util.TreeSet;

public class BaseFrequencyCache<T, T1> {
    public static Integer MAX_CACHE_SIZE = 1000;

    private TreeSet<FrequencyKey<T>> keyFreq = new TreeSet<>();
    private HashMap<T, T1> clientCache = new HashMap<>();

    public BaseFrequencyCache() {
        keyFreq = new TreeSet<>();
        clientCache = new HashMap<>(MAX_CACHE_SIZE);
    }
    public BaseFrequencyCache(int initSize) {
        keyFreq = new TreeSet<>();
        clientCache = new HashMap<>(initSize);
    }
    public void put(T key, T1 value) {
        FrequencyKey<T> newKey = new FrequencyKey<>(key);
        keyFreq.add(newKey);
        clientCache.put(key, value);
    }
    private void putAndEvict(T key, T1 value) {
        if (keyFreq.size() >= MAX_CACHE_SIZE) {
            FrequencyKey<T> remKey = keyFreq.getFirst();
            evict(remKey);
        }
        FrequencyKey<T> newKey = new FrequencyKey<>(key);
        keyFreq.add(newKey);
        clientCache.put(key, value);
    }
    public T1 get(T key) {
        FrequencyKey<T> fKey = new FrequencyKey<>(key);
        FrequencyKey<T> uKey = keyFreq.ceiling(fKey);
        if (uKey == null || !uKey.equals(fKey)) {
            return null;
        }
        uKey.incFrequency();
        return clientCache.get(key);
    }
    public T1 evict(T key) {
        FrequencyKey<T> fKey = new FrequencyKey<>(key);
        return this.evict(fKey);
    }
    public T1 evict(FrequencyKey<T> fKey) {
        keyFreq.remove(fKey);
        return clientCache.remove(fKey);
    }

    public static class FrequencyKey<T> implements Comparable<FrequencyKey<T>> {
        public T uid;
        public int frequency;
        public FrequencyKey(T uid) {
            this.uid = uid;
        }
        public void incFrequency() {
            ++frequency;
        }
        @Override
        public int hashCode() {
            return uid.hashCode();
        }
        @Override
        public boolean equals(Object other) {
            if (!(other instanceof FrequencyKey)) return false;
            if (other == this) return true;
            return this.uid == ((FrequencyKey<?>)other).uid;
        }
        @Override
        public int compareTo(FrequencyKey o) {
            return Integer.compare(frequency, o.frequency);
        }
    }
}