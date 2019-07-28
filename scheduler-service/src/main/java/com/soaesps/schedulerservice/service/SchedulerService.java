package com.soaesps.schedulerservice.service;

import com.soaesps.schedulerservice.domain.SchedulerTask;

import java.time.LocalDateTime;

public interface SchedulerService {
    String composeReport(final LocalDateTime start, final LocalDateTime end);

    boolean registerTask(final SchedulerTask task);
}