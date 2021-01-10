package com.soaesps.schedulerservice.jobs;

import com.soaesps.schedulerservice.service.ReportsServiceI;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportsJob implements Job {
    private static final Logger logger;

    static {
        logger = Logger.getLogger(ReportsJob.class.getName());
        logger.setLevel(Level.INFO);
    }

    @Autowired
    private ReportsServiceI reportsService;

    @Override
    public void execute(JobExecutionContext context) {
        if(logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, String.format("Job ** %1s ** starting @ %2s",
                    context.getJobDetail().getKey().getName(),
                    context.getFireTime()));
        }
        reportsService.composeReport();
        if(logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, String.format("Job ** %1s ** completed. Next job scheduled @ %2s",
                    context.getJobDetail().getKey().getName(),
                    context.getNextFireTime()));
        }
    }
}