package com.soaesps.schedulerservice.service;

import java.time.LocalDateTime;

public interface SchedulerService {
    String composeReport(final LocalDateTime start, final LocalDateTime end);
}