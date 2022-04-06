package com.soaesps.auth.repository;

import com.soaesps.auth.config.HibernateConfiguration;
import com.soaesps.core.DataModels.security.BaseUserDetails;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.Optional;

@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(classes = {HibernateConfiguration.class})
public class UserDetailsRepositoryTest {
    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Before
    public void init() {}

    @Test
    public void A_load_context_test() {
        Assert.assertNotNull(userDetailsRepository);
    }

    @Transactional
    @Rollback(false)
    @Test
    public void B_save_one_test() {
        BaseUserDetails bud = getOneForTest();
        Assert.assertNotNull(bud);
        userDetailsRepository.save(getOneForTest());
    }

    @Transactional
    @Test
    public void C_find_by_user_name() {
        BaseUserDetails testBud = getOneForTest();
        Optional<BaseUserDetails> result = userDetailsRepository.findByUserName(testBud.getUsername());
        Assert.assertTrue(result.isPresent());
        BaseUserDetails bud = result.get();
        Assert.assertTrue(testBud.getUsername().equals(bud.getUsername()));
        Assert.assertTrue(testBud.getPassword().equals(bud.getPassword()));
    }

    @Transactional
    @Rollback(false)
    @Test
    public void D_delete_one_by_name() {
        BaseUserDetails testBud = getOneForTest();
        Assert.assertNotNull(testBud);
        Optional<BaseUserDetails> result = userDetailsRepository.findByUserName(testBud.getUsername());
        Assert.assertTrue(result.isPresent());
        userDetailsRepository.delete(result.get());
    }

    private BaseUserDetails getOneForTest() {
        BaseUserDetails user = new BaseUserDetails();
        user.setCreationTime(ZonedDateTime.now());
        user.setUserName("testUser");
        user.setPassword("password");

        return user;
    }
}