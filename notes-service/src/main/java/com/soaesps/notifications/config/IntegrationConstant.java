package com.soaesps.notifications.config;

public class IntegrationConstant {
    public static final String GATEWAY_CHANNEL = "gateway.channel";

    public static final String FILTER_CHANNEL = "filter.channel";

    public static final String DISCARD_FILTER_CHANNEL = "discard.filter.channel";

    public static final String TRANSFORMER_CHANNEL = "transformer.channel";

    public static final String AGG_ROUTER_CHANNEL = "agg.router.channel";

    public static final String SIMPLE_ROUTER_CHANNEL = "simple.router.channel";

    public static final String MESSAGE_ACTIVATOR_CHANNEL = "message.activator.channel";

    public static final String RECIPIENT_ROUTER_CHANNEL = "recipientRouterChannel";

    // Isolated branch channels for concurrent multi-channel notification streaming
    public static final String EMAIL_BRANCH_CHANNEL = "emailBranchChannel";
    public static final String TELEGRAM_BRANCH_CHANNEL = "telegramBranchChannel";
    public static final String SMS_BRANCH_CHANNEL = "smsBranchChannel";
    public static final String PUSH_BRANCH_CHANNEL = "pushBranchChannel";
    public static final String DEAD_LETTER_CHANNEL = "resolverDlqChannel";

    public static final String AGGREGATOR_CHANNEL = "notificationAggregatorChannel";
}