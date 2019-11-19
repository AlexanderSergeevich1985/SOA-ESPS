package com.soaesps.aggregator.service;

import com.soaesps.aggregator.domain.RegisteredTask;
import com.soaesps.aggregator.repository.RegTaskRepository;
import com.soaesps.core.component.balancer.BaseLoadBalancer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoadBalancer {
    @Autowired
    private RegTaskRepository taskRepository;

    private BaseLoadBalancer loadBalancer;

    @Autowired
    public LoadBalancer(final BaseLoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public void registerJob(final String jobKey) {
        final RegisteredTask task = initTask(jobKey);
        loadBalancer.registerJob(task);
        taskRepository.save(task);
    }

    protected RegisteredTask initTask(final String jobKey) {
        final RegisteredTask task = new RegisteredTask();

        return task;
    }
}