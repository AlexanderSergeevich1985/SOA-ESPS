package service;

import com.soaesps.aggregator.client.IWorkerNode;
import com.soaesps.aggregator.domain.RouterStats;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;

public class Registry {
    private ConcurrentSkipListSet<RouterStats> statistics = new ConcurrentSkipListSet<>();

    private Map<String, IWorkerNode> nodes = new HashMap<>();

    public Registry() {}

    public synchronized void register(final IWorkerNode worker) {
        final RouterStats reference = getRouterStats(worker);
        statistics.add(reference);
    }

    private RouterStats getRouterStats(final IWorkerNode worker) {
        return null;
    }

    public synchronized void unregister(final IWorkerNode worker) {}

    public IWorkerNode getInstance(final String nodeName) {
        return nodes.get(nodeName);
    }
}