package com.soaesps.coordinator.raft;

import com.soaesps.coordinator.dag.CausalDagProcessorI;
import com.soaesps.coordinator.domain.GroupMessage;
import org.apache.ratis.io.MD5Hash;
import org.apache.ratis.protocol.Message;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.server.RaftServer;
import org.apache.ratis.server.protocol.TermIndex;
import org.apache.ratis.server.storage.FileInfo;
import org.apache.ratis.server.storage.RaftStorage;
import org.apache.ratis.statemachine.StateMachineStorage;
import org.apache.ratis.statemachine.TransactionContext;
import org.apache.ratis.statemachine.impl.BaseStateMachine;
import org.apache.ratis.statemachine.impl.SimpleStateMachineStorage;
import org.apache.ratis.statemachine.impl.SingleFileSnapshotInfo;
import org.apache.ratis.thirdparty.com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.Files.newDirectoryStream;

@Component
public class CoordinatorStateMachine extends BaseStateMachine {

    private static final Logger log = LoggerFactory.getLogger(CoordinatorStateMachine.class);

    private final SimpleStateMachineStorage storage = new SimpleStateMachineStorage();
    private final ExecutorService executor;
    private final CausalDagProcessorI dagProcessor;
    private final MessageSerializer messageSerializer;

    private volatile long lastAppliedIndex = 0;
    private volatile long lastAppliedTerm = 0;

    // Snapshot configuration
    private static final long SNAPSHOT_THRESHOLD = 10_000;
    private static final int SNAPSHOT_RETENTION = 3;

    public CoordinatorStateMachine(CausalDagProcessorI dagProcessor,
                                   MessageSerializer messageSerializer) {
        this.dagProcessor = dagProcessor;
        this.messageSerializer = messageSerializer;

        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "coordinator-state-machine");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void initialize(RaftServer raftServer, RaftGroupId groupId, RaftStorage storage) throws IOException {
        super.initialize(raftServer, groupId, storage);
        this.storage.init(storage);

        // Load latest snapshot if exists
        SingleFileSnapshotInfo snapshot = (SingleFileSnapshotInfo) this.storage.getLatestSnapshot();
        if (snapshot != null) {
            TermIndex termIndex = snapshot.getTermIndex();
            this.lastAppliedIndex = termIndex.getIndex();
            this.lastAppliedTerm = termIndex.getTerm();
            log.info("Loaded snapshot at term={}, index={}", lastAppliedTerm, lastAppliedIndex);
        }
    }

    @Override
    public CompletableFuture<Message> applyTransaction(TransactionContext trx) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get message from client request
                Message message = trx.getClientRequest().getMessage();
                long index = trx.getLogEntry().getIndex();
                long term = trx.getLogEntry().getTerm();

                // Deserialize and apply message
                byte[] data = message.getContent().toByteArray();
                GroupMessage groupMessage = messageSerializer.deserialize(data);

                // Update frontier cache with committed message
                dagProcessor.updateFrontier(groupMessage);

                lastAppliedIndex = index;
                lastAppliedTerm = term;

                // Check if snapshot should be taken
                if (index > 0 && index % SNAPSHOT_THRESHOLD == 0) {
                    takeSnapshot();
                }

                log.debug("Applied transaction at term={}, index={}, messageId={}",
                        term, index, groupMessage.id());

                return message;

            } catch (Exception e) {
                log.error("Failed to apply transaction at index={}",
                        trx.getLogEntry().getIndex(), e);
                throw new RuntimeException("Transaction application failed", e);
            }
        }, executor);
    }

    /**
     * Creates a snapshot of the current state machine.
     * Called automatically when index reaches SNAPSHOT_THRESHOLD.
     *
     * @return The index of the snapshot
     */
    @Override
    public long takeSnapshot() {
        try {
            long index = lastAppliedIndex;
            long term = lastAppliedTerm;

            log.info("Creating snapshot at term={}, index={}", term, index);

            // Create snapshot file
            File snapshotFile = storage.getSnapshotFile(term, index);

            // Serialize current state to snapshot file
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(snapshotFile))) {
                // Write metadata
                oos.writeLong(lastAppliedIndex);
                oos.writeLong(lastAppliedTerm);

                log.info("Snapshot metadata saved: index={}, term={}", lastAppliedIndex, lastAppliedTerm);
            }

            MD5Hash fileHash = MD5Hash.newInstance(Files.asByteSource(snapshotFile).read());

            // Create FileInfo with MD5 hash
            FileInfo fileInfo = new FileInfo(snapshotFile.toPath(), fileHash);

            SingleFileSnapshotInfo snapshotInfo = new SingleFileSnapshotInfo(
                    fileInfo,
                    term,
                    index
            );

            // Save snapshot info
            storage.updateLatestSnapshot(snapshotInfo);

            // Clean up old snapshots
            cleanupOldSnapshots();

            log.info("Snapshot created successfully: {}", snapshotFile.getAbsolutePath());

            return index;

        } catch (IOException e) {
            log.error("Failed to create snapshot", e);
            return -1;
        }
    }

    private void cleanupOldSnapshots() {
        try {
            Path snapshotDir = storage.getSnapshotDir().toPath();

            List<SingleFileSnapshotInfo> snapshots = listAllSnapshots(snapshotDir);

            if (snapshots.size() > SNAPSHOT_RETENTION) {
                // Sort by index and remove old ones
                snapshots.sort((a, b) -> Long.compare(
                        a.getTermIndex().getIndex(),
                        b.getTermIndex().getIndex()
                ));

                // Remove oldest snapshots
                for (int i = 0; i < snapshots.size() - SNAPSHOT_RETENTION; i++) {
                    SingleFileSnapshotInfo oldSnapshot = snapshots.get(i);
                    File oldFile = oldSnapshot.getFile().getPath().toFile();;
                    if (oldFile.delete()) {
                        log.info("Deleted old snapshot: {}", oldFile.getName());
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to cleanup old snapshots", e);
        }
    }

    /**
     * Lists all snapshot files in the given directory.
     * This is a replacement for the package-private SimpleStateMachineStorage.getSingleFileSnapshotInfos().
     */
    private List<SingleFileSnapshotInfo> listAllSnapshots(java.nio.file.Path dir) throws IOException {
        List<SingleFileSnapshotInfo> snapshots = new ArrayList<>();

        try (var stream = newDirectoryStream(dir)) {
            for (Path path : stream) {
                Path fileName = path.getFileName();
                if (fileName != null) {
                    // Match snapshot files: snapshot.{term}_{index}
                    String fileNameStr = fileName.toString();
                    if (fileNameStr.startsWith("snapshot.") && !fileNameStr.endsWith(".tmp") && !fileNameStr.endsWith(".corrupt")) {
                        try {
                            // Parse term and index from filename
                            String[] parts = fileNameStr.substring("snapshot.".length()).split("_");
                            if (parts.length == 2) {
                                long term = Long.parseLong(parts[0]);
                                long index = Long.parseLong(parts[1]);

                                // Create FileInfo (MD5 is optional for listing)
                                FileInfo fileInfo = new FileInfo(path, null);

                                // Construct the Ratis shaded single file snapshot info layout
                                snapshots.add(new SingleFileSnapshotInfo(fileInfo, term, index));
                            }
                        } catch (NumberFormatException e) {
                            // Skip files that don't match the expected pattern
                            log.debug("Skipping non-snapshot file due to invalid numeric format: {}", fileName);
                        }
                    }
                }
            }
        }

        return snapshots;
    }

    @Override
    public TermIndex getLastAppliedTermIndex() {
        return TermIndex.valueOf(lastAppliedTerm, lastAppliedIndex);
    }

    @Override
    public StateMachineStorage getStateMachineStorage() {
        return storage;
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}