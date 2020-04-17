package com.soaesps.profile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.core.DataModels.user.UserProfile;
import com.soaesps.profile.ProfileApplication;
import com.soaesps.profile.service.ProfileService;
import com.sun.security.auth.UserPrincipal;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ProfileApplication.class)
@WebAppConfiguration
public class ProfileControllerTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private ProfileController controller;

    @Mock
    private ProfileService service;

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
    public void testGetCurrentUserProfile() throws Exception {
        final UserProfile profile = new UserProfile();
        profile.setUserName("test");

        when(service.getUserProfile(profile.getUserName())).thenReturn(profile);

        mockMvc.perform(get("/profiles/current")
                .principal(new UserPrincipal(profile.getUserName())))
                .andExpect(jsonPath("$.userName").value(profile.getUserName()))
                .andExpect(status().isOk());
    }
}