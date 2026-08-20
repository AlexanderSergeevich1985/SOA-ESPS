package com.soaesps.schedulerservice.controller;

import com.soaesps.schedulerservice.domain.SchedulerTask;
import com.soaesps.schedulerservice.service.SchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchedulerControllerTest {

    @Mock
    private SchedulerService schedulerService;

    private SchedulerController controller;

    @BeforeEach
    void setUp() {
        controller = new SchedulerController(schedulerService);
    }

    @Test
    @DisplayName("composeReport should return 200 with report body when service produces one")
    void composeReport_shouldReturnOkWithReport() {
        OffsetDateTime start = OffsetDateTime.now().minusHours(10);
        OffsetDateTime end = OffsetDateTime.now();
        when(schedulerService.composeReport(start, end)).thenReturn("<html>report</html>");

        ResponseEntity<String> response = controller.composeReport(start, end);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("<html>report</html>");
    }

    @Test
    @DisplayName("composeReport should return 204 No Content when service returns null")
    void composeReport_shouldReturnNoContentWhenServiceReturnsNull() {
        OffsetDateTime start = OffsetDateTime.now().minusHours(10);
        OffsetDateTime end = OffsetDateTime.now();
        when(schedulerService.composeReport(start, end)).thenReturn(null);

        ResponseEntity<String> response = controller.composeReport(start, end);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("composeReport should reject windows where end is before start")
    void composeReport_shouldRejectInvertedWindow() {
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = start.minusHours(5);

        ResponseEntity<String> response = controller.composeReport(start, end);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("registerTask should delegate to the service and return 200")
    void registerTask_shouldDelegateAndReturnOk() {
        SchedulerTask task = new SchedulerTask();
        task.setClassName("cleanup-job");

        ResponseEntity<Void> response = controller.registerTask(task);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(schedulerService).registerTask(task);
    }
}