package com.soaesps.schedulerservice.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service("schedulerServiceImpl")
public class SchedulerServiceImpl implements SchedulerService {
    static private final Logger logger;

    static {
        logger = Logger.getLogger(SchedulerServiceImpl.class.getName());
        logger.setLevel(Level.INFO);
    }

    public String composeReport(final LocalDateTime start, final LocalDateTime end) {
        return "";
    }
}