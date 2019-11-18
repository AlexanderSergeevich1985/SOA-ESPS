package com.soaesps.aggregator.service;

import com.soaesps.core.component.balancer.BaseLoadBalancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoadBalancer {
    private BaseLoadBalancer loadBalancer;
    
    @Autowired
    public LoadBalancer(final BaseLoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }
}