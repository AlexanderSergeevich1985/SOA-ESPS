package com.soaesps.payments.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.payments.config.DatabaseConfig;
import com.soaesps.payments.domain.transactions.BankAccount;
import com.soaesps.payments.domain.transactions.ServerBADesc;
import com.soaesps.payments.service.account.BankAccountService;

import org.junit.Assert;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.UUID;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DatabaseConfig.class}, loader = AnnotationConfigContextLoader.class)
@WebAppConfiguration
public class BankAccountControllerTest {
    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private BankAccountService bankAccountService;

    @InjectMocks
    private BankAccountController controller;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        Assert.assertNotNull(controller);
    }

    @Test
    public void registerBankAccount() throws Exception {
        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/bankaccount/register");
        final String bodyContent = asJsonString(getTestAccount());
        final BankAccount account = mapper.readValue(bodyContent, BankAccount.class);
        Mockito.doReturn(account).when(bankAccountService).registerAccount(Mockito.any());
        this.mockMvc.perform(builder
                .content(bodyContent)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    static protected String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected BankAccount getTestAccount() {
        final BankAccount account = new BankAccount();
        account.setIndentation("test");
        //account.setCreationTime(ZonedDateTime.now());
        final ServerBADesc desc = new ServerBADesc();
        desc.setUuid(UUID.randomUUID());
        desc.setOwnerId(1L);
        desc.setSharedSecret("testSharedSecret");
        account.setServerBADesc(desc);

        return account;
    }
}