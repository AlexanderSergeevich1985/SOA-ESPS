package com.soaesps.payments.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.soaesps.payments.domain.transactions.BankAccount;
import com.soaesps.payments.domain.transactions.ServerBADesc;
import com.soaesps.payments.service.account.BankAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test for {@link BankAccountController}.
 * Uses standalone MockMvc setup without loading Spring ApplicationContext.
 */
@ExtendWith(MockitoExtension.class)
class BankAccountControllerTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Mock
    private BankAccountService bankAccountService;

    @InjectMocks
    private BankAccountController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Standalone setup does not require Spring Context
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Should successfully register a bank account")
    void registerBankAccount_success() throws Exception {
        BankAccount account = getTestAccount();
        when(bankAccountService.registerAccount(any(BankAccount.class))).thenReturn(account);

        mockMvc.perform(post("/bank_account/register")
                        .content(asJsonString(account))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should successfully modify a bank account")
    void modifyBankAccount_success() throws Exception {
        BankAccount account = getTestAccount();
        when(bankAccountService.modifyAccount(any(BankAccount.class))).thenReturn(true);

        mockMvc.perform(put("/bank_account/modify")
                        .content(asJsonString(account))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should successfully delete a bank account")
    void deleteBankAccount_success() throws Exception {
        when(bankAccountService.deleteAccount(any(Long.class))).thenReturn(true);

        mockMvc.perform(delete("/bank_account/delete")
                        .param("accountId", "1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should successfully archive a bank account")
    void archiveBankAccount_success() throws Exception {
        when(bankAccountService.archiveAccount(any(Long.class))).thenReturn(true);

        mockMvc.perform(post("/bank_account/archive")
                        .param("accountId", "1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk());
    }

    private static String asJsonString(final Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (final Exception ex) {
            throw new RuntimeException("Failed to serialize object to JSON", ex);
        }
    }

    private BankAccount getTestAccount() {
        final BankAccount account = new BankAccount();
        account.setIndentation("test");
        account.setCreationTime(ZonedDateTime.now());

        final ServerBADesc desc = new ServerBADesc();
        desc.setUuid(UUID.randomUUID());
        desc.setOwnerId(1L);
        desc.setSharedSecret("testSharedSecret");
        account.setServerBADesc(desc);

        return account;
    }
}