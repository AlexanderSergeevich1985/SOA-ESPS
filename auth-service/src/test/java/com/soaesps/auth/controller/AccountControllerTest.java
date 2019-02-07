package com.soaesps.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.auth.AuthApplication;
import com.soaesps.auth.service.BaseUserDetailsService;
import com.soaesps.core.DataModels.security.BaseUserDetails;
import com.sun.security.auth.UserPrincipal;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AuthApplication.class)
@WebAppConfiguration
public class AccountControllerTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private AccountController controller;

    @Mock
    private BaseUserDetailsService service;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void contexLoads() throws Exception {
        Assert.assertNotNull(controller);
        Assert.assertNotNull(service);
    }

    @Test
    public void testCreateNewAccount() throws Exception {
        final BaseUserDetails user = new BaseUserDetails();
        user.setUserName("test");
        user.setPassword("password");
        String json = mapper.writeValueAsString(user);

        mockMvc.perform(post("/accounts/creation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void testFailCreateNewAccount() throws Exception {
        mockMvc.perform(post("/accounts/creation"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateCurrentAccount() throws Exception {
        final BaseUserDetails user = new BaseUserDetails();
        user.setUserName("test");
        user.setPassword("password");
        String json = mapper.writeValueAsString(user);

        mockMvc.perform(put("/accounts/current")
                .principal(new UserPrincipal(user.getUsername()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }
}