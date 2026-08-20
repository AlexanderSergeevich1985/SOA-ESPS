package com.soaesps.schedulerservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.schedulerservice.dto.FailedDTO;
import com.soaesps.schedulerservice.reports.SchedulerReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link SchedulerReport}.
 *
 * Uses @SpringBootTest to load the full application context so both the
 * Spring-managed ObjectMapper and the Thymeleaf template engine are available.
 * If you want a faster, slice-only test, replace @SpringBootTest with
 * @SpringBootTest(classes = {SchedulerReport.class, SpringTemplateConfig.class}).
 */
@SpringBootTest
class SchedulerReportTest {

    @Autowired
    private SchedulerReport schedulerReport;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== DTO serialization tests ====================

    @Nested
    @DisplayName("FailedDTO JSON serialization")
    class DtoSerializationTests {

        @Test
        @DisplayName("FailedDTO should serialize to valid JSON containing job and timestamps")
        void failedDto_shouldSerializeToJsonWithAllFields() throws JsonProcessingException {
            FailedDTO dto = buildFailedDto();

            String json = objectMapper.writeValueAsString(dto);

            assertThat(json)
                    .isNotBlank()
                    .contains("\"job\"")
                    .contains("\"startTimeStamp\"")
                    .contains("\"endTimeStamp\"");
        }

        @Test
        @DisplayName("FailedDTO should deserialize back from its own JSON (round-trip)")
        void failedDto_shouldRoundTripThroughJson() throws JsonProcessingException {
            FailedDTO original = buildFailedDto();

            String json = objectMapper.writeValueAsString(original);
            FailedDTO restored = objectMapper.readValue(json, FailedDTO.class);

            assertThat(restored)
                    .isNotNull()
                    .usingRecursiveComparison()
                    .ignoringFields("id") // ignore DB-generated fields if any
                    .isEqualTo(original);
        }

        @Test
        @DisplayName("Zoned timestamps should serialize in ISO-8601 format")
        void zonedTimestamps_shouldSerializeAsIso8601() throws JsonProcessingException {
            FailedDTO dto = buildFailedDto();

            String json = objectMapper.writeValueAsString(dto);

            // Spring Boot configures ObjectMapper with JavaTimeModule,
            // which writes ZonedDateTime as ISO-8601 by default
            assertThat(json).matches(".*\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*");
        }
    }

    // ==================== SchedulerReport rendering tests ====================

    @Nested
    @DisplayName("SchedulerReport HTML rendering")
    class HtmlRenderingTests {

        @Test
        @DisplayName("createHtmlReport should render a list of incidents into HTML")
        void createHtmlReport_shouldRenderListOfIncidents() {
            List<FailedDTO> incidents = List.of(buildFailedDto(), buildFailedDto());

            String html = schedulerReport.createHtmlReport(incidents);

            assertThat(html)
                    .isNotBlank()
                    .containsIgnoringCase("<html")
                    .containsIgnoringCase("</html>");
        }

        @Test
        @DisplayName("createHtmlReport should accept a single FailedDTO and still render")
        void createHtmlReport_shouldAcceptSingleIncident() {
            FailedDTO single = buildFailedDto();

            String html = schedulerReport.createHtmlReport(single);

            assertThat(html)
                    .isNotBlank()
                    .containsIgnoringCase("<html");
        }
    }

    // ==================== helpers ====================

    private FailedDTO buildFailedDto() {
        FailedDTO dto = new FailedDTO();
        dto.setJob("test-job-1", new FailedDTO.JobDesc());
        dto.setStartTimeStamp(LocalDateTime.now()
                .minus(Duration.ofHours(10))
                .atZone(ZoneId.of("UTC")));
        dto.setEndTimeStamp(LocalDateTime.now()
                .minus(Duration.ofHours(9))
                .atZone(ZoneId.of("UTC")));
        return dto;
    }
}