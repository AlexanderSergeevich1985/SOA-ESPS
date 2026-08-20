package com.soaesps.schedulerservice.service;

import com.soaesps.schedulerservice.domain.SchedulerTask;
import com.soaesps.schedulerservice.repository.SchedulerTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchedulerServiceImplTest {

    @Mock private TaskScheduler taskScheduler;
    @Mock private SchedulerTaskRepository taskRepository;
    @Mock private ApplicationContext applicationContext;

    private SchedulerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SchedulerServiceImpl(taskScheduler, taskRepository, applicationContext);
    }

    @Test
    @DisplayName("registerTask should persist the task and schedule it via TaskScheduler")
    void registerTask_shouldPersistAndSchedule() {
        SchedulerTask task = buildTask(1, "0 0 2 * * *", "cleanupService", "run");
        Object fakeBean = new Object() { public void run() {} };
        when(applicationContext.getBean("cleanupService")).thenReturn(fakeBean);

        doReturn(mockFuture()).when(taskScheduler)
                .schedule(any(Runnable.class), any(CronTrigger.class));

        boolean result = service.registerTask(task);

        assertThat(result).isTrue();
        verify(taskRepository).save(task);
        verify(taskScheduler).schedule(any(Runnable.class), any(CronTrigger.class));
    }

    @Test
    @DisplayName("registerTask should reject invalid cron expressions")
    void registerTask_shouldRejectInvalidCron() {
        SchedulerTask task = buildTask(1, "not-a-cron", "cleanupService", "run");

        boolean result = service.registerTask(task);

        assertThat(result).isFalse();
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerTask should reject unknown Spring bean names")
    void registerTask_shouldRejectUnknownBean() {
        SchedulerTask task = buildTask(1, "0 0 * * * *", "nonExistentBean", "run");
        when(applicationContext.getBean("nonExistentBean"))
                .thenThrow(new org.springframework.beans.factory.NoSuchBeanDefinitionException("nonExistentBean"));

        boolean result = service.registerTask(task);

        assertThat(result).isFalse();
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerTask should reject missing method on target bean")
    void registerTask_shouldRejectMissingMethod() {
        SchedulerTask task = buildTask(1, "0 0 * * * *", "cleanupService", "noSuchMethod");
        when(applicationContext.getBean("cleanupService")).thenReturn(new Object());

        boolean result = service.registerTask(task);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("cancelTask should return true for active task and false for unknown")
    void cancelTask_shouldCancelActiveAndReturnFalseForUnknown() {
        // register first
        SchedulerTask task = buildTask(1, "0 * * * * *", "svc", "m");
        when(applicationContext.getBean("svc")).thenReturn(new Object() { public void m() {} });

        ScheduledFuture<?> future = mockFuture();
        doReturn(future).when(taskScheduler)
                .schedule(any(Runnable.class), any(CronTrigger.class));
        service.registerTask(task);

        assertThat(service.cancelTask("1")).isTrue();
        assertThat(service.cancelTask("unknown")).isFalse();
        verify(future).cancel(false);
    }

    /**
     * Localizes the unavoidable unchecked raw-type mock into one place.
     */
    @SuppressWarnings("unchecked")
    private ScheduledFuture<?> mockFuture() {
        return mock(ScheduledFuture.class);
    }

    private SchedulerTask buildTask(Integer id, String cron, String bean, String method) {
        SchedulerTask t = new SchedulerTask();
        t.setId(id);
        t.setCronTrigger(cron);
        t.setClassName(bean);
        t.setHandlerName(method);
        return t;
    }
}