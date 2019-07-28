package com.soaesps.schedulerservice.controller;

import com.soaesps.schedulerservice.domain.SchedulerTask;
import com.soaesps.schedulerservice.service.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/scheduler")
public class SchedulerController {
    @Autowired
    @Qualifier("schedulerServiceImpl")
    private SchedulerService schedulerService;

    @GetMapping(path = "/composeReport", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> composeReport(@RequestParam(name = "start") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm") LocalDateTime start, @RequestParam(name = "end", required = false) @DateTimeFormat(pattern="yyyy-MM-dd HH:mm") LocalDateTime end) {
        String report = this.schedulerService.composeReport(start, end);
        if(report != null) {
            return ResponseEntity.ok(report);
        }
        return ResponseEntity.badRequest().body("Invalid data request");
    }

    @PostMapping(path = "/registerTask", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerTask(@Valid @RequestBody SchedulerTask task) {
        schedulerService.registerTask(task);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}