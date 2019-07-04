package com.soaesps.schedulerservice.repository;

import com.soaesps.schedulerservice.SchedulerApplication;
import com.soaesps.schedulerservice.config.DataBaseConfig;
import com.soaesps.schedulerservice.domain.EventAudit;
import com.soaesps.schedulerservice.dto.FailedDTO;
import com.soaesps.schedulerservice.reports.SchedulerReport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
        eventAuditRepository.save(getOneForTest());
    }

    private EventAudit getOneForTest() {
        EventAudit eventAudit = new EventAudit();
        eventAudit.setId(1);
        eventAudit.setEventStatus(EventAudit.EventStatus.SUBMITTED);
        eventAudit.setTimestamp(LocalDateTime.now().atZone(ZoneId.of("UTC")));
        return eventAudit;
    }
}