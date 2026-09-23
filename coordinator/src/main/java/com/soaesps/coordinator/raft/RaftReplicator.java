package com.soaesps.coordinator.raft;

import com.soaesps.coordinator.domain.GroupMessage;
import org.apache.ratis.client.RaftClient;
import org.apache.ratis.protocol.Message;
import org.apache.ratis.protocol.RaftClientReply;
import org.apache.ratis.protocol.exceptions.RaftException;
import org.apache.ratis.protocol.exceptions.TimeoutIOException;
import org.apache.ratis.thirdparty.com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Handles low-level state machine replication to the Apache Ratis cluster.
 * Integrates dynamic particle filter windows directly into Raft consensus barriers.
 */
@Component
public class RaftReplicator {

    private static final Logger log = LoggerFactory.getLogger(RaftReplicator.class);

    private final RaftClient raftClient;
    private final MessageSerializer messageSerializer;

    public RaftReplicator(RaftClient raftClient, MessageSerializer messageSerializer) {
        this.raftClient = raftClient;
        this.messageSerializer = messageSerializer;
    }

    /**
     * Replicates a batch of messages to the Raft cluster.
     * Enforces the dynamic calculation window to maintain consensus liveness.
     *
     * @param batch          The list of causally sorted and conflict-resolved messages.
     * @param timeoutWindowMs The adaptive safety timeout window derived from the NetworkParticleFilter.
     * @return true if successfully committed to the Raft log, false if it should be retried.
     */
    public boolean replicateViaRaft(List<GroupMessage> batch, long timeoutWindowMs) {
        if (batch == null || batch.isEmpty()) {
            return true;
        }

        try {
            // 1. Serialize the batch into a single binary payload for total transactional atomicity
            byte[] payload = messageSerializer.serializeBatch(batch);
            Message raftMessage = Message.valueOf(ByteString.copyFrom(payload));

            // 2. Transmit to the Raft cluster utilizing the dynamic particle filter prediction window.
            // Apache Ratis client allows configuring individual pipeline send time limits.
            RaftClientReply reply = raftClient.async()
                    .send(raftMessage)
                    .orTimeout(timeoutWindowMs, TimeUnit.MILLISECONDS)
                    .join(); // Blocks the coordinator iteration thread until quorum or timeout triggers

            if (reply.isSuccess()) {
                log.debug("Successfully replicated batch of {} messages to Raft quorum. Log index: {}",
                        batch.size(), reply.getLogIndex());
                return true;
            } else {
                log.warn("Raft consensus pipeline rejected append operation. Exception: {}", reply.getException());
                return false;
            }

        } catch (TimeoutIOException | RaftException e) {
            // Captured path during intense inter-datacenter network jitter or split-brain partitions
            log.warn("Raft quorum barrier not cleared within adaptive window ({} ms). Triggers rollback routing.",
                    timeoutWindowMs, e);
            return false;
        } catch (IOException e) {
            log.error("Low-level network I/O error encountered during Raft replication transit", e);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Raft replication worker thread execution context interrupted", e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected unrecoverable exception thrown inside the replication engine", e);
            return false;
        }
    }
}