package com.soaesps.aggregator.service;


import com.soaesps.aggregator.domain.CompletionTask;
import com.soaesps.aggregator.domain.RegisteredTask;

public interface AggregatorServiceI {
    RegisteredTask register(final RegisteredTask task);

    CompletionTask complete(final CompletionTask task);
}