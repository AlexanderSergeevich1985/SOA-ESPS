package com.soaesps.schedulerservice.repository;

import com.soaesps.schedulerservice.config.DataBaseConfig;
import com.soaesps.schedulerservice.domain.EventAudit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DataBaseConfig.class}, loader = AnnotationConfigContextLoader.class)
public class EventAuditRepositoryTest {
    @Autowired
    private EventAuditRepository eventAuditRepository;

    @Before
    public void init() {
        //carService = new CarService(rateFinder);
    }

    @Transactional
    @Test
    public void A_load_context_test() {
        Assert.assertNotNull(eventAuditRepository);
    }

    @Test
    public void B_get_all_test() {
        List<EventAudit> events = eventAuditRepository.findAll();
        Assert.assertNotNull(events);
        print_result(events);
    }

    @Transactional
    @Test
    public void C_save_one_test() {
        eventAuditRepository.save(getOneForTest());
    }

    private EventAudit getOneForTest() {
        EventAudit eventAudit = new EventAudit();
        eventAudit.setEventStatus(EventAudit.EventStatus.SUBMITTED);
        eventAudit.setTimestamp(LocalDateTime.now().atZone(ZoneId.of("UTC")));
        return eventAudit;
    }

    private void print_result(@NotNull final List<EventAudit> events) {
        events.stream().peek(EventAudit::toString).forEach(System.out::println);
    }
}