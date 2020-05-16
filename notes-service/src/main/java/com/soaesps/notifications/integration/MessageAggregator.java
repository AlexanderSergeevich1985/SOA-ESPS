package com.soaesps.notifications.integration;

import com.soaesps.core.component.aggregator.MessageBatch;
import com.soaesps.core.component.aggregator.CorrelationStrategyI;
import com.soaesps.core.component.aggregator.ReleaseStrategyI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.CorrelationStrategy;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ReleaseStrategy;
import org.springframework.messaging.Message;

@MessageEndpoint
public class MessageAggregator {
    private final ReleaseStrategyI releaseStrategy;

    private final CorrelationStrategyI correlationStrategy;

    @Autowired
    public MessageAggregator(final ReleaseStrategyI releaseStrategy,
                             final CorrelationStrategyI correlationStrategy) {
        this.correlationStrategy = correlationStrategy;
        this.releaseStrategy = releaseStrategy;
    }

    @ReleaseStrategy
    public boolean canMessagesBeReleased(final MessageBatch messageBatch) {
        return releaseStrategy.isNeedToRelease(messageBatch);
    }

    @CorrelationStrategy
    public Object getCorrelationKey(final Message<?> message) {
        return correlationStrategy.getCorrelationKey(message)
    }
}