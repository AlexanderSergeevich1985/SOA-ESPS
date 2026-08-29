package com.soaesps.payments.repository;

import com.soaesps.payments.domain.transactions.AccountHistory;
import com.soaesps.payments.domain.transactions.BankAccount;
import com.soaesps.payments.domain.transactions.ServerBADesc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive integration tests for {@link ServerBankAccountRepository}.
 * Inherits infrastructure settings from {@link BasePaymentsRepositoryTest}.
 */
class ServerBankAccountRepositoryTest extends BasePaymentsRepositoryTest {

    @Autowired
    private ServerBankAccountRepository serverBankAccountRepository;

    @Autowired
    private TestEntityManager entityManager;

    private BankAccount testAccount;

    @BeforeEach
    void setUp() {
        testAccount = createTestAccount();
    }

    @Nested
    @DisplayName("Context Loading")
    class ContextTests {

        @Test
        @DisplayName("Repository bean should be successfully injected")
        void repositoryBeanIsAvailable() {
            assertThat(serverBankAccountRepository).isNotNull();
        }
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class CrudTests {

        @Test
        @DisplayName("Should successfully save, retrieve, and delete a BankAccount")
        void saveRetrieveDeleteCycle() {
            // When: Save
            BankAccount saved = serverBankAccountRepository.saveAndFlush(testAccount);
            assertThat(saved.getId()).isNotNull();
            Long id = saved.getId();

            entityManager.clear();

            // Then: Retrieve
            Optional<BankAccount> retrieved = serverBankAccountRepository.findById(id);
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getIndentation()).isEqualTo("test");
            assertThat(retrieved.get().getServerBADesc().getOwnerId()).isEqualTo(1L);
            assertThat(retrieved.get().getHistory()).isNotNull();

            // When: Delete
            serverBankAccountRepository.delete(retrieved.get());
            entityManager.clear();

            // Then: Verify deletion
            assertThat(serverBankAccountRepository.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("Should update existing BankAccount fields")
        void updateExistingAccount() {
            // Given
            BankAccount saved = serverBankAccountRepository.saveAndFlush(testAccount);
            entityManager.clear();

            // When
            BankAccount toUpdate = serverBankAccountRepository.findById(saved.getId()).orElseThrow();
            toUpdate.setIndentation("updated-test-indentation");
            serverBankAccountRepository.saveAndFlush(toUpdate);
            entityManager.clear();

            // Then
            BankAccount updated = serverBankAccountRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.getIndentation()).isEqualTo("updated-test-indentation");
        }
    }

    @Nested
    @DisplayName("Primary Key Join & Cascade Mapping")
    class PrimaryKeyJoinTests {

        @Test
        @DisplayName("Should cascade persist AccountHistory and force it to share the BankAccount ID")
        void saveAccount_shouldCascadeAndSharePrimaryKey() {
            // When
            BankAccount savedAccount = serverBankAccountRepository.saveAndFlush(testAccount);
            Long accountId = savedAccount.getId();

            entityManager.clear();

            // Then
            BankAccount retrievedAccount = serverBankAccountRepository.findById(accountId).orElse(null);
            assertThat(retrievedAccount).isNotNull();
            assertThat(retrievedAccount.getHistory()).isNotNull();

            // Verify PrimaryKeyJoinColumn constraint (Shared ID with AccountHistory)
            assertThat(retrievedAccount.getHistory().getAccountId()).isEqualTo(accountId);
        }
    }

    @Nested
    @DisplayName("Validation and Null Constraints")
    class ConstraintTests {

        @Test
        @DisplayName("Should throw exception when indentation is null")
        void nullIndentation_shouldFailValidation() {
            // Given
            testAccount.setIndentation(null);

            // Then
            assertThatThrownBy(() -> serverBankAccountRepository.saveAndFlush(testAccount));
        }

        @Test
        @DisplayName("Should throw exception when billSignature is null")
        void nullBillSignature_shouldFailValidation() {
            // Given
            testAccount.setBillSignature(null);

            // Then
            assertThatThrownBy(() -> serverBankAccountRepository.saveAndFlush(testAccount));
        }
    }

    /**
     * Factory method to construct a valid pre-populated BankAccount with required entities.
     */
    private BankAccount createTestAccount() {
        BankAccount account = new BankAccount();
        account.setIndentation("test");
        account.setCreationTime(ZonedDateTime.now());
        account.setBillSignature(new byte[]{1, 2, 3});

        ServerBADesc desc = new ServerBADesc();
        desc.setUuid(UUID.randomUUID());
        desc.setOwnerId(1L);
        desc.setAccountBalance(BigDecimal.ZERO);
        desc.setOwnerPublicKey(new byte[]{1});
        desc.setPublicKey(new byte[]{2});
        desc.setPrivateKey(new byte[]{3});
        desc.setCipherKey(new byte[]{4});

        account.setServerBADesc(desc);

        // Required to avoid PropertyValueException since history is mandatory (optional = false)
        AccountHistory history = new AccountHistory();
        account.setHistory(history);
        history.setBankAccount(account);

        return account;
    }
}