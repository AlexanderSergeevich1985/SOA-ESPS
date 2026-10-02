package com.soaesps.jetdag.controller;

import com.soaesps.jetdag.service.PipelineOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dag")
@RequiredArgsConstructor
@Slf4j
public class DagTriggerController {

    private final PipelineOrchestratorService orchestratorService;

    /**
     * Authenticated endpoint to trigger a DAG execution on-demand.
     * Fully reactive, non-blocking pipeline fetching, validation, and Temporal handoff.
     *
     * @param pipelineId    The unique ID of the pipeline stored in PostgreSQL configuration tables.
     * @param principalMono The reactive security context containing authenticated user/agent JWT claims.
     * @return A map containing status and the unique Temporal Workflow Execution ID.
     */
    @PostMapping("/run/{pipelineId}")
    public Mono<ResponseEntity<Map<String, String>>> triggerDag(
            @PathVariable UUID pipelineId,
            @AuthenticationPrincipal Mono<Jwt> principalMono) {

        return principalMono
                // 1. Extract context from authenticated token (works perfectly with microservice resource servers)
                .flatMap(jwt -> {
                    String userId = jwt.getSubject();
                    String username = jwt.getClaimAsString("preferred_username");
                    log.info("User [{}] (ID: {}) initiated on-demand run for DAG [{}]", username, userId, pipelineId);

                    // 2. Delegate the graph composition, validation, and dispatching to the service layer
                    return orchestratorService.triggerPipeline(pipelineId);
                })
                // 3. Map successful Temporal workflow spawn to an HTTP 200 response payload
                .map(workflowId -> ResponseEntity.ok(Map.of(
                        "status", "TRIGGERED",
                        "message", "DAG configuration loaded from DB and handed off to Temporal successfully",
                        "temporal_workflow_id", workflowId
                )))
                // 4. Graceful reactive handling for invalid architectures (e.g., loops detected by DagValidator)
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    log.error("Graph validation or business constraints failed for pipeline [{}]: {}", pipelineId, ex.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body(Map.of("error", ex.getMessage())));
                })
                // 5. Catch-all fallback for internal connectivity errors (e.g., DB down, Temporal cluster unreachable)
                .onErrorResume(Exception.class, ex -> {
                    log.error("Critical failure during reactive initialization of DAG [{}]: ", pipelineId, ex);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
}