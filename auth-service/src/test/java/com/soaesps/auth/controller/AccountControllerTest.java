package com.soaesps.auth.controller;

import com.soaesps.auth.service.BaseUserDetailsService;
import com.soaesps.core.DataModels.security.BaseUserDetails;
import com.soaesps.core.Utils.JsonUtil;
import com.soaesps.core.security.repository.AuthAuditRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.security.auth.UserPrincipal;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(AccountController.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AccountControllerTest {
    @MockBean
    private AuthAuditRepository authAuditRepository;

    @MockBean
    private BaseUserDetailsService userDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setup() {
        initMocks(this);
        Mockito.doReturn(getTestUserDetails()).when(userDetailsService).loadUserByUsername(Mockito.anyString());
    }

    @Test
    public void A_contexLoads() {
        Assert.assertNotNull(userDetailsService);
        Assert.assertNotNull(mockMvc);
    }

    @WithMockUser(value = "testUser", roles = "ADMIN")
    @Test
    public void B_getCurrentUser_test() throws Exception {
        MvcResult result = mockMvc.perform(get("/accounts/load")
                .principal(new UserPrincipal("testUser")))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        BaseUserDetails userDetails = JsonUtil.fromString(content, BaseUserDetails.class);
        Assert.assertNotNull(userDetails);
    }

    @WithMockUser(value = "testUser", roles = "ADMIN")
    @Test
    public void C_createUserAccount_test() throws Exception {
        Mockito.doReturn(1L).when(userDetailsService).createUserAccount(Mockito.any());
        BaseUserDetails userDetails = getTestUserDetails();
        String content = JsonUtil.toString(userDetails);
        MvcResult result = mockMvc.perform(post("/accounts/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String id = result.getResponse().getHeader("entity_id");
        Assert.assertEquals(Long.valueOf(id).longValue(), 1L);
    }

    @WithMockUser(value = "testUser", roles = "ADMIN")
    @Test
    public void D_updateCurrentUser_test() throws Exception {
        BaseUserDetails userDetails = getTestUserDetails();
        String content = JsonUtil.toString(userDetails);
        mockMvc.perform(put("/accounts/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithMockUser(value = "testUser", roles = "ADMIN")
    @Test
    public void E_changePassword_test() throws Exception {
        mockMvc.perform(put("/accounts/change_password")
                .header("old_password","oldPassword")
                .header("new_password","newPassword")
                .principal(new UserPrincipal("testUser"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithMockUser(value = "testUser", roles = "ADMIN")
    @Test
    public void F_removeUser_test() throws Exception {
        mockMvc.perform(delete("/accounts/remove")
                .principal(new UserPrincipal("testUser"))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithMockUser(value = "testUser")
    @Test
    public void G_IsExist_failed_test() throws Exception {
        mockMvc.perform(get("/accounts/isExist/{username}", "testUser"))
                .andExpect(status().isBadRequest());
    }


    @WithMockUser(value = "testUser", roles = "ADMIN")
    @Test
    public void H_IsExist_test() throws Exception {
        mockMvc.perform(get("/accounts/isExist/{username}", "testUser"))
                .andExpect(status().isOk());
    }

    private BaseUserDetails getTestUserDetails() {
        final BaseUserDetails user = new BaseUserDetails();
        user.setUsername("test_user");
        user.setPassword("password");

        return user;
    }
}