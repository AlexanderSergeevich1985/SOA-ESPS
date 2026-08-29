package com.soaesps.payments.repository;

import com.soaesps.payments.DataModels.Transactions.BaseServerBill;
import com.soaesps.payments.DataModels.Transactions.ServerBillDesc;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive integration tests for {@link ServerBillsRepository}.
 */
class ServerBillsRepositoryTest extends BasePaymentsRepositoryTest {

    @Autowired
    private ServerBillsRepository serverBillsRepository;

    @Autowired
    private TestEntityManager entityManager;

    private BaseServerBill testBill;

    @BeforeEach
    void setUp() {
        testBill = createTestBill();
    }

    @Nested
    @DisplayName("Context Loading")
    class ContextTests {

        @Test
        @DisplayName("Repository bean should be successfully injected")
        void repositoryBeanIsAvailable() {
            assertThat(serverBillsRepository).isNotNull();
        }
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class CrudTests {

        @Test
        @DisplayName("Should successfully save, retrieve, and delete a ServerBill")
        void saveRetrieveDeleteCycle() {
            // When: Save
            BaseServerBill saved = serverBillsRepository.saveAndFlush(testBill);
            assertThat(saved.getId()).isNotNull();
            Long id = saved.getId();

            entityManager.clear();

            // Then: Retrieve
            Optional<BaseServerBill> retrieved = serverBillsRepository.findById(id);
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getIndentation()).isEqualTo("test-bill-indentation");
            assertThat(retrieved.get().getServerBillDesc().getOwnerId()).isEqualTo("test-owner-id-string-value");
            assertThat(retrieved.get().getBillSignature()).containsExactly(10, 20, 30);

            // When: Delete
            serverBillsRepository.delete(retrieved.get());
            entityManager.clear();

            // Then: Verify deletion
            assertThat(serverBillsRepository.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("Should update existing ServerBill fields")
        void updateExistingBill() {
            // Given
            BaseServerBill saved = serverBillsRepository.saveAndFlush(testBill);
            entityManager.clear();

            // When
            BaseServerBill toUpdate = serverBillsRepository.findById(saved.getId()).orElseThrow();
            toUpdate.setIndentation("updated-bill-indentation");
            serverBillsRepository.saveAndFlush(toUpdate);
            entityManager.clear();

            // Then
            BaseServerBill updated = serverBillsRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.getIndentation()).isEqualTo("updated-bill-indentation");
        }
    }

    @Nested
    @DisplayName("Validation and Constraints")
    class ConstraintTests {

        @Test
        @DisplayName("Should throw exception when indentation exceeds maximum allowed size")
        void indentationTooLong_shouldFailValidation() {
            // Given: string size > 256 characters
            String longIndentation = "a".repeat(257);
            testBill.setIndentation(longIndentation);

            // Then
            assertThatThrownBy(() -> serverBillsRepository.saveAndFlush(testBill))
                    .isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("Should throw exception when indentation is null")
        void nullIndentation_shouldFailValidation() {
            // Given
            testBill.setIndentation(null);

            // Then
            assertThatThrownBy(() -> serverBillsRepository.saveAndFlush(testBill));
        }

        @Test
        @DisplayName("Should throw exception when billSignature is null")
        void nullBillSignature_shouldFailValidation() {
            // Given
            testBill.setBillSignature(null);

            // Then
            assertThatThrownBy(() -> serverBillsRepository.saveAndFlush(testBill));
        }
    }

    /**
     * Factory method to construct a valid pre-populated BaseServerBill with required embedded properties.
     */
    private BaseServerBill createTestBill() {
        BaseServerBill bill = new BaseServerBill();
        bill.setIndentation("test-bill-indentation");
        bill.setBillSignature(new byte[]{10, 20, 30});

        ServerBillDesc desc = new ServerBillDesc();
        desc.setUuid(UUID.randomUUID());
        desc.setOwnerId("test-owner-id-string-value");
        // Assuming typical fields for Bill description similar to BankAccount
        desc.setAccountBalance(BigDecimal.TEN);
        desc.setOwnerPublicKey(new byte[]{1});
        desc.setPublicKey(new byte[]{2});
        desc.setPrivateKey(new byte[]{3});
        desc.setCipherKey(new byte[]{4});

        bill.setServerBillDesc(desc);
        return bill;
    }
}