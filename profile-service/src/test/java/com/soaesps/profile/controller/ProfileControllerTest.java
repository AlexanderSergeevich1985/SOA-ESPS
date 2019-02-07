package com.soaesps.profile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.profile.ProfileApplication;
import com.soaesps.profile.service.ProfileService;
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

import static org.mockito.MockitoAnnotations.initMocks;

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
}
