package com.soaesps.schedulerservice.controller;

import com.soaesps.schedulerservice.domain.SchedulerTask;
import com.soaesps.schedulerservice.service.SchedulerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.OffsetDateTime;

/**
 * REST API for the scheduler microservice.
 *
 * <p>Exposes two endpoints:
 * <ul>
 *   <li>{@code GET /scheduler/composeReport} — generates an HTML/JSON report of failed jobs
 *       within a time window;</li>
 *   <li>{@code POST /scheduler/registerTask} — registers a new scheduled task in Quartz.</li>
 * </ul>
 */
@RestController
@RequestMapping("/scheduler")
public class SchedulerController {

    private static final Logger log = LoggerFactory.getLogger(SchedulerController.class);

    private final SchedulerService schedulerService;

    public SchedulerController(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    /**
     * Generates a report of failed scheduler jobs between two instants.
     *
     * @param start required lower bound of the reporting window (ISO-8601)
     * @param end   optional upper bound; defaults to "now"
     */
    @PreAuthorize("hasRole('SERVICE')")
    @GetMapping(path = "/composeReport", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> composeReport(
            @RequestParam(name = "start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,

            @RequestParam(name = "end", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end) {

        OffsetDateTime effectiveEnd = end != null ? end : OffsetDateTime.now();

        if (!effectiveEnd.isAfter(start)) {
            log.warn("Invalid report window: start={}, end={}", start, effectiveEnd);
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Parameter 'end' must be strictly after 'start'");
            problem.setTitle("Invalid reporting window");
            problem.setType(URI.create("about:blank"));
            problem.setProperty("start", start);
            problem.setProperty("end", effectiveEnd);
            return ResponseEntity.badRequest().body(problem.toString());
        }

        log.info("Composing scheduler report for window [{}, {}]", start, effectiveEnd);
        String report = schedulerService.composeReport(start, effectiveEnd);

        if (report == null) {
            // Service returned null: treated as "no data" (204 No Content), not 400 Bad Request.
            log.debug("No incidents found in window [{}, {}]", start, effectiveEnd);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(report);
    }

    /**
     * Registers a new scheduled task in the Quartz engine.
     */
    @PreAuthorize("hasRole('SERVICE')")
    @PostMapping(path = "/registerTask", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> registerTask(@Valid @RequestBody SchedulerTask task) {
        log.info("Registering scheduler task: {}", task.getClassName());
        schedulerService.registerTask(task);
        return ResponseEntity.ok().build();
    }
}