package com.soaesps.payments.repository;

import com.soaesps.payments.domain.transactions.BankAccount;
import com.soaesps.payments.domain.transactions.ServerBADesc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link ServerBankAccountRepository}.
 * Uses @DataJpaTest to automatically configure an in-memory database and JPA repositories
 * without loading the full Spring ApplicationContext. Transactions are rolled back automatically after each test.
 */
@DataJpaTest
class ServerBankAccountRepositoryTest {

    @Autowired
    private ServerBankAccountRepository serverBankAccountRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should save BankAccount, assign ID, and persist to database")
    void save_shouldPersistAccountAndAssignId() {
        // Given
        BankAccount account = getTestAccount();

        // When
        BankAccount savedAccount = serverBankAccountRepository.save(account);

        // Flush to force Hibernate to execute SQL INSERT, and clear to detach entities from L1 cache
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(savedAccount.getId()).isNotNull();

        Optional<BankAccount> retrieved = serverBankAccountRepository.findById(savedAccount.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getIndentation()).isEqualTo("test");
        assertThat(retrieved.get().getServerBADesc().getOwnerId()).isEqualTo(1L);
    }

    protected BankAccount getTestAccount() {
        BankAccount account = new BankAccount();
        account.setIndentation("test");
        account.setCreationTime(ZonedDateTime.now());

        // FIXED: BaseBankAccount requires billSignature (nullable = false)
        account.setBillSignature(new byte[]{1, 2, 3});

        ServerBADesc desc = new ServerBADesc();
        desc.setUuid(UUID.randomUUID());
        desc.setOwnerId(1L);

        // FIXED: ServerBADesc requires several non-null fields based on previous class definition.
        // Without these, Hibernate will throw PropertyValueException on flush.
        desc.setAccountBalance(BigDecimal.ZERO);
        desc.setOwnerPublicKey(new byte[]{1});
        desc.setPublicKey(new byte[]{2});
        desc.setPrivateKey(new byte[]{3});
        desc.setCipherKey(new byte[]{4});

        account.setServerBADesc(desc);

        return account;
    }
}