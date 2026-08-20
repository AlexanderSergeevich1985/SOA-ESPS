package com.soaesps.schedulerservice.service;

import com.soaesps.schedulerservice.domain.SchedulerTask;
import com.soaesps.schedulerservice.repository.SchedulerTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Default implementation of {@link SchedulerService}.
 *
 * <p>Schedules tasks by looking up Spring beans by name and invoking a public no-arg method.
 * This design is significantly safer than reflection-based execution: arbitrary classes from the
 * classpath cannot be instantiated, and the target must be a managed Spring bean.
 */
@Service("schedulerServiceImpl")
public class SchedulerServiceImpl implements SchedulerService {

    private static final Logger log = LoggerFactory.getLogger(SchedulerServiceImpl.class);

    private final TaskScheduler taskScheduler;
    private final SchedulerTaskRepository taskRepository;
    private final ApplicationContext applicationContext;

    /** Tracks active scheduled futures by task name so they can be cancelled later. */
    private final Map<String, ScheduledFuture<?>> activeFutures = new ConcurrentHashMap<>();

    public SchedulerServiceImpl(TaskScheduler taskScheduler,
                                SchedulerTaskRepository taskRepository,
                                ApplicationContext applicationContext) {
        this.taskScheduler = taskScheduler;
        this.taskRepository = taskRepository;
        this.applicationContext = applicationContext;
    }

    @Override
    public String composeReport(OffsetDateTime start, OffsetDateTime end) {
        if (start == null || end == null) {
            return null;
        }
        log.debug("Composing report for window [{}, {}]", start, end);
        // TODO: delegate to SchedulerReport / report generator bean
        return "";
    }

    /**
     * Registers a new scheduled task: persists to DB, validates the cron expression
     * and the target bean/method, then schedules execution.
     *
     * @return true if the task was successfully registered, false otherwise
     */
    @Override
    @Transactional
    public boolean registerTask(SchedulerTask task) {
        if (task == null || task.getClassName() == null) {
            log.warn("Attempted to register a task with missing name");
            return false;
        }

        // 1. Validate cron expression before touching the DB
        if (!isValidCron(task.getCronTrigger())) {
            log.warn("Invalid cron expression '{}' for task '{}'", task.getCronTrigger(), task.getClassName());
            return false;
        }

        // 2. Validate that the target bean + method exist BEFORE persisting
        Runnable runnable;
        try {
            runnable = buildRunnable(task.getClassName(), task.getHandlerName());
        } catch (TaskRegistrationException ex) {
            log.warn("Task '{}' cannot be registered: {}", task.getClassName(), ex.getMessage());
            return false;
        }

        try {
            taskRepository.save(task);

            // Cancel any previously running instance with the same name
            cancelTask(task.getClassName());

            CronTrigger trigger = new CronTrigger(task.getCronTrigger());
            ScheduledFuture<?> future = taskScheduler.schedule(runnable, trigger);
            activeFutures.put(task.getClassName(), future);

            log.info("Registered task '{}' with cron '{}'", task.getClassName(), task.getCronTrigger());
            return true;
        } catch (Exception ex) {
            log.error("Failed to register task '{}'", task.getClassName(), ex);
            throw ex; // let @Transactional roll back the DB save
        }
    }

    /**
     * Cancels a previously registered task by name.
     *
     * @return true if a task with the given name was active and has been cancelled
     */
    public boolean cancelTask(String taskName) {
        ScheduledFuture<?> future = activeFutures.remove(taskName);
        if (future == null) {
            return false;
        }
        boolean cancelled = future.cancel(false);
        log.info("Cancelled task '{}' (mayInterruptIfRunning=false): {}", taskName, cancelled);
        return cancelled;
    }

    /**
     * Builds a Runnable that invokes a public no-arg method on a Spring bean.
     *
     * <p>SECURITY: unlike the previous reflection-based approach, the target MUST be
     * a registered Spring bean (resolved by name). Arbitrary classes cannot be instantiated,
     * eliminating the remote-code-execution surface.
     *
     * @throws TaskRegistrationException if the bean or method does not exist
     */
    protected Runnable buildRunnable(String beanName, String methodName) {
        if (beanName == null || beanName.isBlank()) {
            throw new TaskRegistrationException("beanName must not be blank");
        }
        if (methodName == null || methodName.isBlank()) {
            throw new TaskRegistrationException("methodName must not be blank");
        }

        Object bean;
        try {
            bean = applicationContext.getBean(beanName);
        } catch (org.springframework.beans.factory.NoSuchBeanDefinitionException ex) {
            throw new TaskRegistrationException("Spring bean '" + beanName + "' not found", ex);
        }

        java.lang.reflect.Method method;
        try {
            method = bean.getClass().getMethod(methodName);
        } catch (NoSuchMethodException ex) {
            throw new TaskRegistrationException(
                    "No public no-arg method '" + methodName + "' on bean '" + beanName + "'", ex);
        }

        return () -> {
            try {
                method.invoke(bean);
            } catch (Exception ex) {
                log.error("Scheduled task '{}.{}' failed", beanName, methodName, ex);
            }
        };
    }

    private boolean isValidCron(String expression) {
        if (expression == null || expression.isBlank()) {
            return false;
        }
        try {
            CronExpression.parse(expression);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Thrown when a task cannot be registered due to invalid bean/method reference.
     */
    public static class TaskRegistrationException extends RuntimeException {
        public TaskRegistrationException(String message) { super(message); }
        public TaskRegistrationException(String message, Throwable cause) { super(message, cause); }
    }
}