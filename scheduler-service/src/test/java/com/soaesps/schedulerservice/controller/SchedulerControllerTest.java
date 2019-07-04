package com.soaesps.schedulerservice.controller;

import com.soaesps.schedulerservice.SchedulerApplication;
import com.soaesps.schedulerservice.repository.EventAuditRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = SchedulerApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:application.yaml")
public class SchedulerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventAuditRepository repository;

    @Test
    public void A_context_loads_test() {}
}