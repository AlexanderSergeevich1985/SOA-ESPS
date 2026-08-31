package com.soaesps.notifications.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.soaesps.notifications.dto.BranchStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Aggregator;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.List;

import static com.soaesps.notifications.config.IntegrationConstant.AGGREGATOR_CHANNEL;

@Configuration
public class NotificationStatusAggregator {
    private static final Logger log = LoggerFactory.getLogger(NotificationStatusAggregator.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Aggregates completed branch payloads into a single unified execution report.
     * Fires automatically when total collected messages match the sequenceSize header.
     */
    @Aggregator(inputChannel = AGGREGATOR_CHANNEL,
            outputChannel = "kafkaOutboundChannel")
    public Message<ObjectNode> aggregateStatuses(List<BranchStatus> branchStatuses) {
        log.info("All parallel notification branches completed execution. Compiling final status report.");

        ObjectNode finalReport = objectMapper.createObjectNode();
        if (branchStatuses.isEmpty()) {
            finalReport.put("status", "MUTED_ALL_CHANNELS");
            return MessageBuilder.withPayload(finalReport).build();
        }

        Long userId = branchStatuses.get(0).getUserId();
        finalReport.put("userId", userId);
        finalReport.put("status", "DISPATCH_COMPLETED");

        ArrayNode details = finalReport.putArray("dispatchDetails");
        for (BranchStatus branch : branchStatuses) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("channel", branch.getChannelName());
            node.put("result", branch.getResultStatus());
            details.add(node);
        }

        return MessageBuilder.withPayload(finalReport).build();
    }
}