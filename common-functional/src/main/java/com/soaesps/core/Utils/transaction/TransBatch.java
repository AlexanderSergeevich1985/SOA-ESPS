package com.soaesps.core.Utils.transaction;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.RecursiveAction;

public class TransBatch<T extends Number> {
    private TransPersister<T> persister;

    private PriorityQueue<Transaction<T>> queue;

    private BigDecimal value;

    public TransBatch() {
        this.queue = new PriorityQueue<>(getComparator());
    }

    public TransPersister<T> getPersister() {
        return persister;
    }

    public void setPersister(final TransPersister<T> persister) {
        this.persister = persister;
    }

    public static <T extends Number> Comparator<Transaction<T>> getComparator() {
        return (o1, o2) -> {
            if (!o1.getType().equals(o2.getType())) {
                if (o1.getType().equals(TransDesc.Type.AUGMENTED)) {
                    return 1;
                }
                else {
                    return -1;
                }
            }

            return o1.getZdt().compareTo(o2.getZdt());
        };
    }

    public class BatchTask extends RecursiveAction {
        public void compute() {
            while (!queue.isEmpty()) {
                final Transaction transaction = queue.poll();
                BigDecimal newValue = value.add(new BigDecimal(transaction.getValue().toString()));
                if (newValue.compareTo(new BigDecimal(0.0)) == -1) {
                    return;
                }
                persister.save(transaction.getDesc());
                value = newValue;
            }
        }
    }
}