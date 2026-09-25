package com.soaesps.coordinator.config;

import com.soaesps.coordinator.raft.CoordinatorStateMachine;
import org.apache.ratis.client.RaftClient;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.grpc.GrpcConfigKeys;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.protocol.RaftPeerId;
import org.apache.ratis.rpc.SupportedRpcType;
import org.apache.ratis.server.RaftServer;
import org.apache.ratis.server.RaftServerConfigKeys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.UUID;

@Configuration
public class RaftConfig {

    @Value("${raft.server.id:coordinator-1}")
    private String serverId;

    @Value("${raft.server.port:9876}")
    private int serverPort;

    @Value("${raft.client.port:9877}")
    private int clientPort;

    @Value("${raft.group.id:soa-esps-group}")
    private String groupId;

    @Bean
    public RaftProperties raftProperties() {
        RaftProperties properties = new RaftProperties();

        // Set RPC type to gRPC
        properties.setEnum("raft.rpc.type", SupportedRpcType.GRPC);

        // gRPC server configuration
        GrpcConfigKeys.Server.setPort(properties, serverPort);

        // Snapshot configuration
        RaftServerConfigKeys.Snapshot.setAutoTriggerEnabled(properties, true);
        RaftServerConfigKeys.Snapshot.setAutoTriggerThreshold(properties, 10_000L);
        RaftServerConfigKeys.Snapshot.setRetentionFileNum(properties, 3);

        // Log compaction
        RaftServerConfigKeys.Log.setPurgeGap(properties, 10_000);

        // Performance tuning
        RaftServerConfigKeys.Write.setElementLimit(properties, 1024 * 1024);

        return properties;
    }

    @Bean
    public RaftGroupId raftGroupId() {
        return RaftGroupId.valueOf(UUID.fromString(groupId));
    }

    @Bean
    public RaftPeer raftPeer() {
        return RaftPeer.newBuilder()
                .setId(RaftPeerId.valueOf(serverId))
                .setAddress(new InetSocketAddress("localhost", serverPort))
                .setClientAddress(new InetSocketAddress("localhost", clientPort))
                .build();
    }

    @Bean
    public RaftGroup raftGroup(RaftGroupId raftGroupId, RaftPeer raftPeer) {
        return RaftGroup.valueOf(raftGroupId, Collections.singletonList(raftPeer));
    }

    /**
     * FIX: Use RaftServer.newBuilder() instead of RaftServerProxy (which is package-private)
     */
    @Bean(initMethod = "start", destroyMethod = "close")
    public RaftServer raftServer(CoordinatorStateMachine stateMachine,
                                 RaftProperties properties,
                                 RaftGroup raftGroup) throws Exception {
        return RaftServer.newBuilder()
                .setServerId(RaftPeerId.valueOf(serverId))
                .setGroup(raftGroup)
                .setProperties(properties)
                .setStateMachine(stateMachine)
                .build();
    }

    @Bean
    public RaftClient raftClient(RaftProperties properties, RaftGroup raftGroup) {
        return RaftClient.newBuilder()
                .setRaftGroup(raftGroup)
                .setProperties(properties)
                .build();
    }
}