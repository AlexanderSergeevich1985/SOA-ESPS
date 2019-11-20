package com.soaesps.aggregator.service;

import com.soaesps.aggregator.domain.RegisteredTask;
import com.soaesps.aggregator.dto.TaskRequest;
import com.soaesps.aggregator.repository.RegTaskRepository;
import com.soaesps.core.component.balancer.BaseLoadBalancer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.RecursiveAction;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class LoadBalancer {
    @Autowired
    private RegTaskRepository taskRepository;

    private BaseLoadBalancer loadBalancer;

    private static final Logger logger;

    static {
        logger = Logger.getLogger(LoadBalancer.class.getName());
        logger.setLevel(Level.INFO);
    }

    final static ForkJoinPool.ForkJoinWorkerThreadFactory factory = pool -> {
        final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
        worker.setName("index_" + worker.getPoolIndex());

        return worker;
    };

    private ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors(), factory, new ExceptionHandler(), true);

    @Autowired
    public LoadBalancer(final BaseLoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public boolean registerJob(final String jobKey) {
        final RegisteredTask task = initTask(jobKey);
        if (task == null || task.getTaskId() == null) {
            return false;
        }
        loadBalancer.registerJob(new TaskRequest(task.getTaskId(), jobKey));
        forkJoinPool.execute(this.new DistJob());

        return true;
    }

    protected RegisteredTask initTask(final String jobKey) {
        final RegisteredTask task = new RegisteredTask();

        return taskRepository.save(task);
    }

    public static class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(final Thread t, final Throwable e) {
            logger.log(Level.SEVERE, "[LoadBalancer/ExceptionHandler] thread name:".concat(t.getName()).concat(" runtime exception occurred:").concat(e.getMessage()));
        }
    }

    public class DistJob extends RecursiveAction {
        @Override
        protected void compute() {
            final TaskRequest payload = (TaskRequest) loadBalancer.startOneJob();
            final RegisteredTask task = taskRepository.findById(payload.getTaskId()).orElse(null);
            if (task == null) {
                return;
            }
            payload.setWorkerNodeId(task.getWorkerNodeId());
            payload.setReservedNodeId(task.getReservedNodeId());

            taskRepository.save(task);
        }
    }
}