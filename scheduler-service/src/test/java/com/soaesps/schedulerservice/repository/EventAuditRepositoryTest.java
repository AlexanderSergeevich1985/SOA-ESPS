package com.soaesps.schedulerservice.repository;

import com.soaesps.schedulerservice.config.DataBaseConfig;
import com.soaesps.schedulerservice.domain.EventAudit;
import com.soaesps.schedulerservice.dto.FailedDTO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
//@TransactionConfiguration(defaultRollback = false)
@ContextConfiguration(classes = {DataBaseConfig.class}, loader = AnnotationConfigContextLoader.class)
public class EventAuditRepositoryTest {
    @Autowired
    private EventAuditRepository eventAuditRepository;

    @Before
    public void init() {
        //carService = new CarService(rateFinder);
    }

    @Test
    public void A_load_context_test() {
        Assert.assertNotNull(eventAuditRepository);
    }

    @Transactional
    //@Rollback(false)
    @Test
    public void B_get_all_test() {
        List<EventAudit> events = eventAuditRepository.findAll();
        EventAudit event = events.get(0);
        event.setEventStatus(EventAudit.EventStatus.REPORTED);
        eventAuditRepository.save(getOneForTest());
        Assert.assertNotNull(events);
        print_result(events);
    }

    @Transactional
    @Rollback(false)
    @Test
    public void C_save_one_test() throws InterruptedException {
        eventAuditRepository.save(getOneForTest());
        TimeUnit.MILLISECONDS.sleep(1000);
    }

    private EventAudit getOneForTest() {
        EventAudit eventAudit = new EventAudit();
        eventAudit.setEventStatus(EventAudit.EventStatus.SUBMITTED);
        eventAudit.setTimestamp(LocalDateTime.now().atZone(ZoneId.of("UTC")));
        eventAudit.setFailedDTO(getFailedDto());
        return eventAudit;
    }

    private void print_result(@NotNull final List<EventAudit> events) {
        events.stream().peek(EventAudit::toString).forEach(System.out::println);
    }

    private FailedDTO getFailedDto() {
        FailedDTO dto = new FailedDTO();

        dto.setJob("1", new FailedDTO.JobDesc());

        dto.setStartTimeStamp(LocalDateTime.now().minus(Duration.ofHours(10)).atZone(ZoneId.of("UTC")));
        dto.setEndTimeStamp(LocalDateTime.now().minus(Duration.ofHours(9)).atZone(ZoneId.of("UTC")));


        return dto;
    }
}