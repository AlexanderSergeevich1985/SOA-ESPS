package com.soaesps.schedulerservice.Utils.quartz;

import com.soaesps.core.Utils.DateTimeHelper;
import com.soaesps.schedulerservice.config.SchedulerConstants;
import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

public class QuartzUtils {
    public static SimpleTriggerFactoryBean createTrigger(JobDetail jobDetail, String triggerName, long delay) {
        return QuartzUtils.SimpleTriggerBuilder.builder()
                .jobDetail(jobDetail)
                .name(triggerName)
                .startDelay(delay)
                .repeatInterval(SchedulerConstants.POLL_FREQUENCY_MS)
                .repeatCount(SimpleTrigger.REPEAT_INDEFINITELY)
                .misfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT)
                .getFactoryBean();
    }

    public static CronTriggerFactoryBean createCronTrigger(JobDetail jobDetail, String cronExpression, String triggerName) {
        return QuartzUtils.CronTriggerBuilder.builder()
                .jobDetail(jobDetail)
                .cronExpression(cronExpression)
                .name(triggerName)
                .startTime(DateTimeHelper.localDateTimeToDate(LocalDateTime.now().with(LocalTime.MIN), null))
                .startDelay(SchedulerConstants.DEFAULT_START_DELAY)
                .misfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT)
                .getFactoryBean();
    }

    public static JobDetailFactoryBean createJobDetail(Class jobClass, String jobName) {
        return JobDetailBuilder.builder()
                .jobClass(jobClass)
                .name(jobName)
                .durability(true)
                .getFactoryBean();
    }

    public static class JobDetailBuilder {
        private JobDetailFactoryBean factoryBean;

        public static JobDetailBuilder builder() {
            JobDetailBuilder builder = new JobDetailBuilder();
            builder.factoryBean = new JobDetailFactoryBean();

            return builder;
        }

        public JobDetailBuilder name(String jobName) {
            factoryBean.setName(jobName);

            return this;
        }

        public JobDetailBuilder durability(boolean isDurable) {
            factoryBean.setDurability(isDurable);

            return this;
        }

        public JobDetailBuilder jobClass(Class jobClass) {
            factoryBean.setJobClass(jobClass);

            return this;
        }

        public JobDetailBuilder group(String group) {
            factoryBean.setGroup(group);

            return this;
        }

        public JobDetailBuilder appContext(ApplicationContext appContext) {
            factoryBean.setApplicationContextJobDataKey(SchedulerConstants.APP_CONTEXT_JOB_DATA_KEY);
            factoryBean.setApplicationContext(appContext);

            return this;
        }

        public JobDetailFactoryBean getFactoryBean() {
            return factoryBean;
        }

        public JobDetail build() {
            factoryBean.afterPropertiesSet();

            return factoryBean.getObject();
        }
    }

    public static class CronTriggerBuilder {
        private CronTriggerFactoryBean factoryBean;

        public static CronTriggerBuilder builder() {
            CronTriggerBuilder builder = new CronTriggerBuilder();
            builder.factoryBean = new CronTriggerFactoryBean();

            return builder;
        }

        public CronTriggerBuilder name(String jobName) {
            factoryBean.setName(jobName);

            return this;
        }

        public CronTriggerBuilder startTime(Date time) {
            factoryBean.setStartTime(time);

            return this;
        }

        public CronTriggerBuilder startDelay(Long delay) {
            factoryBean.setStartDelay(delay);

            return this;
        }

        public CronTriggerBuilder misfireInstruction(int instruction) {
            factoryBean.setMisfireInstruction(instruction);

            return this;
        }

        public CronTriggerBuilder jobDetail(JobDetail jobDetail) {
            factoryBean.setJobDetail(jobDetail);

            return this;
        }

        public CronTriggerBuilder cronExpression(String cronExpression) {
            factoryBean.setCronExpression(cronExpression);

            return this;
        }

        public CronTriggerFactoryBean getFactoryBean() {
            return factoryBean;
        }

        public CronTrigger build() throws ParseException {
            factoryBean.afterPropertiesSet();

            return factoryBean.getObject();
        }
    }

    public static class SimpleTriggerBuilder {
        private SimpleTriggerFactoryBean factoryBean;

        public static SimpleTriggerBuilder builder() {
            SimpleTriggerBuilder builder = new SimpleTriggerBuilder();
            builder.factoryBean = new SimpleTriggerFactoryBean();

            return builder;
        }

        public SimpleTriggerBuilder name(String jobName) {
            factoryBean.setName(jobName);

            return this;
        }

        public SimpleTriggerBuilder startTime(Date time) {
            factoryBean.setStartTime(time);

            return this;
        }

        public SimpleTriggerBuilder startDelay(Long delay) {
            factoryBean.setStartDelay(delay);

            return this;
        }

        public SimpleTriggerBuilder misfireInstruction(int instruction) {
            factoryBean.setMisfireInstruction(instruction);

            return this;
        }

        public SimpleTriggerBuilder jobDetail(JobDetail jobDetail) {
            factoryBean.setJobDetail(jobDetail);

            return this;
        }

        public SimpleTriggerBuilder repeatInterval(long interval) {
            factoryBean.setRepeatInterval(interval);

            return this;
        }

        public SimpleTriggerBuilder repeatCount(int count) {
            factoryBean.setRepeatCount(count);

            return this;
        }

        public SimpleTriggerFactoryBean getFactoryBean() {
            return factoryBean;
        }

        public SimpleTrigger build() {
            factoryBean.afterPropertiesSet();

            return factoryBean.getObject();
        }
    }
}