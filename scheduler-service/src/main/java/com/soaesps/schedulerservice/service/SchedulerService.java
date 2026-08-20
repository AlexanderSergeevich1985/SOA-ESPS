package com.soaesps.schedulerservice.service;

import com.soaesps.schedulerservice.domain.SchedulerTask;

import java.time.OffsetDateTime;

public interface SchedulerService {
    String composeReport(final OffsetDateTime start, final OffsetDateTime end);

    boolean registerTask(final SchedulerTask task);
}