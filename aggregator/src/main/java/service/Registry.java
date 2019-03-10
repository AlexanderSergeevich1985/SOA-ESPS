package service;

import com.soaesps.aggregator.client.IWorkerNode;
import com.soaesps.aggregator.domain.RouterStats;

import java.util.concurrent.ConcurrentSkipListSet;

public class Registry {
    private ConcurrentSkipListSet<RouterStats> statistics = new ConcurrentSkipListSet<>();

    public synchronized void register(final IWorkerNode worker) {
        final RouterStats reference = getRouterStats(worker);
        statistics.add(reference);
    }

    private RouterStats getRouterStats(final IWorkerNode worker) {
        return null;
    }

    public synchronized void unregister(final IWorkerNode worker) {}
}
