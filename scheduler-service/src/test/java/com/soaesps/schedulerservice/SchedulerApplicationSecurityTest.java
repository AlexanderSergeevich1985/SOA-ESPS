package com.soaesps.schedulerservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SchedulerApplicationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Unauthenticated request should be rejected at TLS level or 401")
    void unauthenticated_shouldBeRejected() throws Exception {
        mockMvc.perform(get("/scheduler/composeReport")
                        .param("start", OffsetDateTime.now().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Health endpoint should be publicly accessible")
    void health_shouldBePublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}