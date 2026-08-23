package com.soaesps.schedulerservice.config;

import org.quartz.Trigger;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

/**
 * Quartz scheduler configuration with JDBC job store and cluster support.
 */
@Configuration
public class QuartzConfig {

    private final ApplicationContext applicationContext;
    private final DataSource dataSource;

    public QuartzConfig(ApplicationContext applicationContext, DataSource dataSource) {
        this.applicationContext = applicationContext;
        this.dataSource = dataSource;
    }

    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean scheduler(List<Trigger> triggers) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();

        Properties properties = new Properties();
        properties.setProperty("org.quartz.scheduler.instanceName", "soa-esps-scheduler");
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        // Cluster mode: nodes coordinate via the shared QRTZ_* tables
        properties.setProperty("org.quartz.jobStore.isClustered", "true");
        properties.setProperty("org.quartz.jobStore.clusterCheckinInterval", "15000");
        properties.setProperty("org.quartz.threadPool.threadCount", "10");

        schedulerFactory.setOverwriteExistingJobs(true);
        schedulerFactory.setAutoStartup(true);
        schedulerFactory.setQuartzProperties(properties);
        schedulerFactory.setDataSource(dataSource);
        schedulerFactory.setJobFactory(springBeanJobFactory());
        schedulerFactory.setWaitForJobsToCompleteOnShutdown(true);

        if (triggers != null && !triggers.isEmpty()) {
            schedulerFactory.setTriggers(triggers.toArray(new Trigger[0]));
        }

        return schedulerFactory;
    }

    /**
     * Job factory that injects @Autowired fields/setters into Quartz job instances.
     *
     * NOTE: required only while jobs use FIELD injection. The default SpringBeanJobFactory
     * in Spring 6 already resolves CONSTRUCTOR dependencies, so once jobs are migrated
     * to constructor injection this class can be deleted.
     */
    public static final class AutowiringSpringBeanJobFactory
            extends SpringBeanJobFactory implements ApplicationContextAware {

        private AutowireCapableBeanFactory beanFactory;

        @Override
        public void setApplicationContext(final ApplicationContext context) {
            beanFactory = context.getAutowireCapableBeanFactory();
        }

        @Override
        protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
            final Object job = super.createJobInstance(bundle);
            beanFactory.autowireBean(job);
            return job;
        }
    }
}