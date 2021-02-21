package com.soaesps.core.Utils.operation;

import com.soaesps.core.Utils.DataStructure.LimitedQueue;
import com.soaesps.core.Utils.DataStructure.QueueI;

import java.io.Serializable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

public class Exchanger<T extends Serializable> {
    private QueueI<ExchangeTask> values;

    private AtomicReference<T> ref;

    private Supplier<T> loader;

    private Function<T, T> writer;

    public Exchanger() {
        this.values = new LimitedQueue<>();
        ref = new AtomicReference<>();
    }

    public T getValue() {
        if (ref.get() == null) {
            loadValue();
        }

        return ref.get();
    }

    synchronized protected void loadValue() {
        T result = ref.get();
        if (result != null) {
            return;
        }
        ref.set(loader.get());
    }

    public boolean save(T updated) {
        ExchangeTask task = new ExchangeTask(updated, null);

        return values.push(task);
    }

    protected boolean update(T update) {
        try {
            T updated = writer.apply(update);
            if (updated != null) {
                this.ref.set(updated);

                return true;
            } else {
                onFailure(update, null);
            }
        } catch (Exception ex) {
            onFailure(update, ex);
        }

        return false;
    }

    protected void onFailure(T update, Exception ex) {}

    private class ExchangeSupplier implements Supplier<T> {
        private T update;

        public ExchangeSupplier(T update) {
            this.update = update;
        }

        @Override
        public T get() {
            if (update == null) {
                return null;
            }
            update(update);

            return update;
        }
    }

    private class ExchangeTask extends ForkJoinTask<T> {
        private T value;

        Supplier<T> supplier;

        CompletableFuture<T> future;

        public ExchangeTask(T updated, Supplier<T> supplier) {
            this.value = updated;
            this.supplier = supplier;
        }

        @Override
        public T getRawResult() {
            return value;
        }

        @Override
        public void setRawResult(T v) {}

        @Override
        public final boolean exec() {
            future = CompletableFuture.supplyAsync(supplier);

            return true;
        }
    }
}