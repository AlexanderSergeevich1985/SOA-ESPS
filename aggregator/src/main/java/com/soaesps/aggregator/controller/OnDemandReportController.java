package com.soaesps.aggregator.controller;

import com.soaesps.aggregator.domain.UserAdviceEvent;
import com.soaesps.aggregator.report.PeriodicReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Reactive REST Endpoint for ad-hoc / on-demand summary generation requested directly by the user.
 * Built on top of Spring WebFlux to guarantee non-blocking execution.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class OnDemandReportController {

    private final PeriodicReportService reportService;

    /**
     * Triggers immediate 6h aggregate analysis for a specific user reactively.
     * Fires the reactive pipeline in the background and instantly returns 202 Accepted.
     */
    @PostMapping("/user/{userId}/trigger")
    public Mono<ResponseEntity<Void>> triggerOnDemandReport(@PathVariable Long userId) {
        log.info("Received reactive manual on-demand report request for user={}", userId);

        // Fire-and-forget pattern in Project Reactor:
        // We subscribe to the stream on the boundedElastic scheduler so the HTTP Netty thread is released instantly.
        reportService.generateSingleUserReportReactive(userId, UserAdviceEvent.TRIGGER_ON_DEMAND)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        null, // OnNext: void, nothing to emit
                        ex -> log.error("Background reactive on-demand report failed for user={}", userId, ex),
                        () -> log.info("Background reactive on-demand report pipeline completed for user={}", userId)
                );

        return Mono.just(ResponseEntity.accepted().build());
    }
}