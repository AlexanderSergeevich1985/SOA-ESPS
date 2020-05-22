package com.soaesps.core.integration;

public class IntegrationConstant {
    public static final String GATEWAY_CHANNEL = "gateway.channel";

    public static final String FILTER_CHANNEL = "filter.channel";

    public static final String DISCARD_FILTER_CHANNEL = "discard.filter.channel";

    public static final String TRANSFORMER_CHANNEL = "transformer.channel";

    public static final String AGG_ROUTER_CHANNEL = "router.channel";

    public static final String SIMPLE_ROUTER_CHANNEL = "router.channel";

    public static final String MESSAGE_ACTIVATOR_CHANNEL = "message.activator.channel";

    public enum Exchanges {
        AUTH_OUEUE("soa-esps-authenticate", ""),
        USER_DETAILS_OUEUE("soa-esps-authenticate", ""),
        TOKEN_OUEUE("soa-esps-authenticate", "");

        private String exchangeName;

        private String routeKey;

        Exchanges(final String exchangeName, final String routeKey) {
            this.exchangeName = exchangeName;
            this.routeKey = routeKey;
        }

        public String getExchangeName() {
            return exchangeName;
        }

        public String getRouteKey() {
            return routeKey;
        }
    }
}