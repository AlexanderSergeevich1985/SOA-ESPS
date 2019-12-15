package com.soaesps.payments.repository;

import com.soaesps.payments.config.DatabaseConfig;
import com.soaesps.payments.domain.transactions.BankAccount;
import com.soaesps.payments.domain.transactions.ServerBADesc;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.time.ZonedDateTime;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
//@TransactionConfiguration(defaultRollback = false)
@ContextConfiguration(classes = {DatabaseConfig.class}, loader = AnnotationConfigContextLoader.class)
public class ServerBankAccountRepositoryTest {
    @Autowired
    private ServerBankAccountRepository serverBankAccountRepository;

    @Test
    public void A_save() {
        BankAccount account = getTestAccount();
        account = serverBankAccountRepository.save(account);
        Assert.assertNotNull(account);
        Assert.assertTrue(serverBankAccountRepository.existsById(account.getId()));
    }

    protected BankAccount getTestAccount() {
        BankAccount account = new BankAccount();
        account.setIndentation("test");
        account.setCreationTime(ZonedDateTime.now());
        ServerBADesc desc = new ServerBADesc();
        desc.setOwnerId(1L);
        account.setServerBADesc(desc);

        return account;
    }
}