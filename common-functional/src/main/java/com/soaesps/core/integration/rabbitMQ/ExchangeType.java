package com.soaesps.core.integration.rabbitMQ;

public enum ExchangeType {
    DIRECT("direct"),
    TOPIC("topic"),
    FANOUT("fanout"),
    HEADER("headers"),
    CONSISTENT_HASH("consistent_hash"),
    EXCHANGE_TO_EXCHANGE("exchange_to_exchange");

    private final String exchangeName;

    ExchangeType(final String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getExchangeName() {
        return this.exchangeName;
    }
}