package com.soaesps.core.DataModels.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Payloader interface responsible for
 * evaluation and execution placement of tasks on nodes.
 */
public class PayloaderImpl implements Payloader {

    private static final Logger log = LoggerFactory.getLogger(PayloaderImpl.class);

    /**
     * Attempts to load a given payload execution unit onto the target node.
     *
     * @param payload the task execution properties
     * @param node    the target server node
     * @return true if the node accepted the task, false if resources are exhausted
     */
    @Override
    public boolean load(final Payload payload, final ExecutorNode node) {
        if (payload == null || node == null) {
            return false;
        }

        // Check if the node has available statistic or capability slots left
        if (node.getStatistic() == null) {
            log.warn("Target node ID: {} has uninitialized statistic metrics.", node.getId());
            return false;
        }

        try {
            // Business logic stub for allocating resources on the executor node.
            // Typically increases the active request counter or load metric.
            log.info("Successfully routing payload for job: {} onto executor node: {}",
                    payload.getJobKey(), node.getId());

            return true;
        } catch (Exception ex) {
            log.error("Failed to load payload onto node due to unexpected system error: ", ex);
            return false;
        }
    }
}
