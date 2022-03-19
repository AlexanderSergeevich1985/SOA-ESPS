package com.soaesps.schedulerservice.config;

public class SchedulerConstants {
    public static final String CRON_EVERY_FIVE_MINUTES = "0 0/5 * ? * * *";

    public static final Long DEFAULT_START_DELAY = 0L;

    public static final Long POLL_FREQUENCY_MS = 60000L;

    public static final String APP_CONTEXT_JOB_DATA_KEY = "applicationContext";
}