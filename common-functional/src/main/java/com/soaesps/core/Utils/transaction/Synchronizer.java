package com.soaesps.core.Utils.transaction;

import com.soaesps.core.Utils.DataStructure.LRUInMemoryCache;

import java.util.concurrent.atomic.AtomicBoolean;

public class Synchronizer<ID extends Comparable<ID>, T> {
    private LRUInMemoryCache<ID, TransWithLock<T>> onReadCache;

    private LRUInMemoryCache<ID, Transaction<T>> onUpdateCache;

    private TransPersister<ID, T> persister;

    public Synchronizer(final TransPersister<ID, T> persister) {
        this.onReadCache = new LRUInMemoryCache<>();
        this.onUpdateCache = new LRUInMemoryCache<>();
        this.persister = persister;
    }

    public Transaction<T> getRecord(final ID key, final boolean isNeedNew) {
        final TransWithLock<T> oldRecord = this.onReadCache.get(key);
        if (oldRecord == null) {
            return persister.load(key);
        } else {
            if (!isNeedNew || !oldRecord.isUpdated()) {
                return oldRecord.transaction;
            } else {
                return onUpdateCache.get(key);
            }
        }
    }

    synchronized public void update(final ID key, final Transaction<T> transaction) {
        onUpdateCache.addWithEvict(key, transaction);
        final TransWithLock<T> oldRecord = onReadCache.get(key);
        if (oldRecord != null) {
            oldRecord.setState(true);
        }
        final Transaction<T> newRecord = persister.save(transaction);
        if (oldRecord != null) {
            oldRecord.setTransaction(newRecord);
        }
        onUpdateCache.remove(key);
    }

    public static class TransWithLock<T> {
        private Transaction<T> transaction;

        private AtomicBoolean flag;

        public TransWithLock(final Transaction<T> transaction) {
            this.transaction = transaction;
        }

        public void setState(final boolean state) {
            if (this.flag == null) {
                this.flag = new AtomicBoolean(state);
            }
        }

        public boolean isUpdated() {
            if (flag == null) {
                return false;
            }

            return flag.get();
        }

        public void setTransaction(final Transaction<T> transaction) {
            this.transaction = transaction;
            setState(false);
        }
    }
}