package com.soaesps.schedulerservice.repository;

import com.soaesps.schedulerservice.SchedulerApplication;
import com.soaesps.schedulerservice.config.DataBaseConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;


@RunWith(SpringJUnit4ClassRunner.class)
//@RunWith(SpringRunner.class)
//@DataJpaTest
@ActiveProfiles("junit")
@SpringBootTest(classes = {DataBaseConfig.class})
//@SpringApplicationConfiguration(classes = SchedulerApplication.class)
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EventAuditRepositoryTest {
    @Autowired
    DataSource restDataSource;
    ///private EventAuditRepository eventAuditRepository;

    @Before
    public void init() {
        //carService = new CarService(rateFinder);
    }


    @Test
    public void A_load_context_test() {
        //Moc
    }
}