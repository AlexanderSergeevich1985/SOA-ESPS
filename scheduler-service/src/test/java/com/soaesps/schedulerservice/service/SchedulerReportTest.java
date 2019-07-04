package com.soaesps.schedulerservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.schedulerservice.config.SpringTemplateConfig;
import com.soaesps.schedulerservice.dto.FailedDTO;
import com.soaesps.schedulerservice.reports.SchedulerReport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringTemplateConfig.class, SchedulerReport.class}, loader=AnnotationConfigContextLoader.class)
public class SchedulerReportTest {
    @Autowired
    private SchedulerReport schedulerReport;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void convert_withOut_Exception() throws IOException {
        System.out.println(mapper.writeValueAsString(getFailedDto()));
    }

    private FailedDTO getFailedDto() {
        FailedDTO dto = new FailedDTO();

        dto.setJob("1", new FailedDTO.JobDesc());

        dto.setStartTime(LocalDateTime.now().minus(Duration.ofHours(10)));
        dto.setEndTime(LocalDateTime.now().minus(Duration.ofHours(9)));


        return dto;
    }
}