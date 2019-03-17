package service;

import com.soaesps.aggregator.client.IWorkerNode;

public class LoadBalancer implements ILoadBalancer {
    private Registry registry;

    public LoadBalancer() {}

    public LoadBalancer(final Registry registry) {
    }

    public LoadBalancer(final IWorkerNode... nodes) {
        for (IWorkerNode node: nodes) {
            registry.register(node);
        }
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }


    public IWorkerNode getBestWorker() {
        return this.getRegistry().getInstance("");
    }
}