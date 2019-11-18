package com.soaesps.aggregator.config;

import com.soaesps.core.component.balancer.BaseLoadBalancer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadBalancerConfig {
    @Bean
    public BaseLoadBalancer loadBalancer() {
        return new BaseLoadBalancer();
    }
}