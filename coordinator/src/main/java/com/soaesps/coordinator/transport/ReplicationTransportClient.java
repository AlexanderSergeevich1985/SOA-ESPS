package com.soaesps.coordinator.transport;

import com.soaesps.coordinator.domain.GroupMessage;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Networking interface responsible for replicating message batches
 * to remote peer coordinator servers across data centers.
 */
public interface ReplicationTransportClient {

    /**
     * Asynchronously replicates a batch of messages to a specific remote coordinator node.
     *
     * @param targetServerUrl The destination network address of the peer coordinator.
     * @param iteration       The current iteration marker of the batch.
     * @param messages        The causally ordered batch of messages.
     * @return A future indicating the success or failure of the network transit.
     */
    CompletableFuture<Void> replicateBatch(String targetServerUrl, long iteration, List<GroupMessage> messages);
}