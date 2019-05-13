package com.soaesps.payments.service;

import com.soaesps.payments.repository.ServerBillsRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

//@SpringBootTest
//@DataJpaTest
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AccountApplication.class)
public class PaymentsServiceImplTest {
    //@Autowired
    //private ServerBillsRepository serverBillsRepository;

    @Test
    public void A_test() {}
}