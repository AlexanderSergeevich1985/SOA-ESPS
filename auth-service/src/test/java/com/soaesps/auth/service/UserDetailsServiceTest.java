package com.soaesps.auth.service;

import com.soaesps.auth.repository.UserDetailsRepository;
import com.soaesps.core.DataModels.security.BaseUserDetails;
import com.soaesps.core.security.repository.AuthAuditRepository;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.mockito.MockitoAnnotations.initMocks;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserDetailsServiceTest {
    @MockBean
    private AuthAuditRepository authAuditRepository;

    @MockBean
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private BaseUserDetailsService baseUserDetailsService;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void A_contextLoads() {
        Assert.assertNotNull(baseUserDetailsService);
    }

    @Test(expected = UsernameNotFoundException.class)
    public void B_loadUserByUsername_not_found_test() {
        baseUserDetailsService.loadUserByUsername("testUser");
    }

    @Test
    public void C_loadUserByUsername_test() {
        Mockito.doReturn(Optional.of(getTestBaseUserDetails())).when(userDetailsRepository)
                .findByUsername(Mockito.anyString());
        UserDetails userDetails = baseUserDetailsService.loadUserByUsername("testUser");
        Assert.assertNotNull(userDetails);
    }

    @Test
    public void D_createUserAccount_test() {
        BaseUserDetails bud = getTestBaseUserDetails();
        baseUserDetailsService.createUserAccount(bud);
    }

    @Test
    public void E_updateUserAccount_test() {
        BaseUserDetails bud = getTestBaseUserDetails();
        Mockito.doReturn(Optional.of(bud)).when(userDetailsRepository)
                .findByUsername(Mockito.anyString());
        bud.setPassword("newTestPassword");
        baseUserDetailsService.updateUserAccount("testUser", bud);
    }

    @Test
    public void F_deleteUserAccount_test() {
        Mockito.doReturn(Optional.of(getTestBaseUserDetails())).when(userDetailsRepository)
                .findByUsername(Mockito.anyString());
        Assert.assertTrue(baseUserDetailsService.deleteUserAccount("testUser"));
    }

    @Test
    public void G_changePassword_test() {
        Mockito.doReturn(Optional.of(getTestBaseUserDetails())).when(userDetailsRepository)
                .findByUsername(Mockito.any());
        baseUserDetailsService.changePassword("password", "newTestPassword");
    }

    @Test
    public void H_changePassword_test() {
        Mockito.doReturn(Optional.of(getTestBaseUserDetails())).when(userDetailsRepository)
                .findByUsername(Mockito.anyString());
        Assert.assertTrue(baseUserDetailsService.userExists("testUser"));
    }

    private BaseUserDetails getTestBaseUserDetails() {
        BaseUserDetails bud = new BaseUserDetails();
        bud.setUsername("testUser");
        bud.setPassword("password");

        return bud;
    }
}