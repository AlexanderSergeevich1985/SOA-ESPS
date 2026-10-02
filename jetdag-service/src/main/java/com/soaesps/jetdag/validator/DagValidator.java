package com.soaesps.jetdag.validator;

import com.soaesps.jetdag.dto.PipelineDto;
import com.soaesps.jetdag.dto.TaskDto;

import java.util.*;

public final class DagValidator {

    private DagValidator() {
        // Prevent instantiation of a static utility class
    }

    /**
     * Validates if the given pipeline data structure represents a true Directed Acyclic Graph (DAG).
     * Automatically scans for back-edges and structural circular dependencies.
     *
     * @param pipeline The unified pipeline data transfer object containing tasks and dependencies.
     * @throws IllegalArgumentException if an architectural cycle or loop is detected within the graph.
     */
    public static void validate(PipelineDto pipeline) {
        if (pipeline.tasks() == null || pipeline.tasks().isEmpty()) {
            return; // Empty graph is structurally safe by default
        }

        // 1. Build an Adjacency List for fast node traversal (Upstream TaskCode -> List of Downstream TaskCodes)
        Map<String, List<String>> adjacencyList = new HashMap<>();
        for (TaskDto task : pipeline.tasks()) {
            adjacencyList.put(task.taskCode(), new ArrayList<>());
        }

        // 2. Map UUID IDs to human-readable string Codes for clean traversal debugging
        Map<UUID, String> idToCodeMap = new HashMap<>();
        for (TaskDto task : pipeline.tasks()) {
            idToCodeMap.put(task.id(), task.taskCode());
        }

        // 3. Populate the adjacency list based on relationships loaded from PostgreSQL
        for (var edge : pipeline.dependencies()) {
            String parentCode = idToCodeMap.get(edge.upstreamTaskId());
            String childCode = idToCodeMap.get(edge.downstreamTaskId());

            if (parentCode != null && childCode != null) {
                adjacencyList.get(parentCode).add(childCode);
            }
        }

        // 4. Track visitation status: 0 = UNVISITED, 1 = VISITING (currently in call stack), 2 = COMPLETED
        Map<String, Integer> visitationStates = new HashMap<>();
        for (TaskDto task : pipeline.tasks()) {
            visitationStates.put(task.taskCode(), 0);
        }

        // 5. Trigger DFS scan from every unvisited root node to handle disconnected graph networks
        for (TaskDto task : pipeline.tasks()) {
            if (visitationStates.get(task.taskCode()) == 0) {
                if (hasCycleDfs(task.taskCode(), adjacencyList, visitationStates)) {
                    throw new IllegalArgumentException(
                            String.format("Critical architectural violation: Circular dependency (infinite loop) detected in pipeline [%s]!", pipeline.name())
                    );
                }
            }
        }
    }

    private static boolean hasCycleDfs(String currentNode, Map<String, List<String>> adjacencyList, Map<String, Integer> states) {
        // Mark current node as VISITING (pushed to runtime execution stack)
        states.put(currentNode, 1);

        List<String> neighbors = adjacencyList.getOrDefault(currentNode, Collections.emptyList());
        for (String neighbor : neighbors) {
            Integer neighborState = states.get(neighbor);

            if (neighborState == 1) {
                return true; // Back-edge detected! Neighbor is already in the current stack, which proves a loop exists.
            }

            if (neighborState == 0) {
                // Recursively descend down the reactive dependency chain
                if (hasCycleDfs(neighbor, adjacencyList, states)) {
                    return true;
                }
            }
        }

        // Mark node as COMPLETED (popped from stack, fully verified as safe)
        states.put(currentNode, 2);
        return false;
    }
}