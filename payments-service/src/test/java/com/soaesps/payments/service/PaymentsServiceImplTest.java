package com.soaesps.payments.service;

import com.soaesps.payments.DataModels.Transactions.BaseServerBill;
import com.soaesps.payments.DataModels.Transactions.ServerBillDesc;
import com.soaesps.payments.repository.BasePaymentsRepositoryTest;
import com.soaesps.payments.repository.ServerBillsRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive integration tests for {@link ServerBillsRepository}.
 * Inherits isolated Spring Data JPA configuration from {@link BasePaymentsRepositoryTest}.
 */
class ServerBillsRepositoryTest extends BasePaymentsRepositoryTest {

    @Autowired
    private ServerBillsRepository serverBillsRepository;

    @Autowired
    private TestEntityManager entityManager;

    private BaseServerBill testBill;
    private UUID testBillUuid;

    @BeforeEach
    void setUp() {
        testBillUuid = UUID.randomUUID();
        testBill = createTestBill(testBillUuid);
    }

    @Nested
    @DisplayName("Context Loading")
    class ContextTests {

        @Test
        @DisplayName("Repository bean should be successfully injected into the context")
        void repositoryBeanIsAvailable() {
            assertThat(serverBillsRepository).isNotNull();
        }
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class CrudTests {

        @Test
        @DisplayName("Should successfully save, retrieve by ID, and delete a ServerBill")
        void saveRetrieveDeleteCycle() {
            // When: Save the bill using the repository
            BaseServerBill saved = serverBillsRepository.save(testBill);
            assertThat(saved.getId()).isNotNull();
            Long id = saved.getId();

            entityManager.flush();
            entityManager.clear();

            // Then: Retrieve the bill by its Long database ID
            Optional<BaseServerBill> retrieved = serverBillsRepository.findById(id);
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getIndentation()).isEqualTo("test-bill-indentation");
            assertThat(retrieved.get().getServerBillDesc().getOwnerId()).isEqualTo("test-owner-id-string-value");
            assertThat(retrieved.get().getBillSignature()).containsExactly(10, 20, 30);

            // When: Delete the entity from the database
            serverBillsRepository.delete(retrieved.get());
            entityManager.flush();
            entityManager.clear();

            // Then: Verify it no longer exists
            assertThat(serverBillsRepository.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("Should update fields of an existing ServerBill")
        void updateExistingBill() {
            // Given: Pre-persisted bill
            BaseServerBill saved = serverBillsRepository.saveAndFlush(testBill);
            entityManager.clear();

            // When: Modifying the indentation property
            BaseServerBill toUpdate = serverBillsRepository.findById(saved.getId()).orElseThrow();
            toUpdate.setIndentation("updated-bill-indentation-value");
            serverBillsRepository.saveAndFlush(toUpdate);
            entityManager.clear();

            // Then: Verify changes are persisted correctly
            BaseServerBill updated = serverBillsRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.getIndentation()).isEqualTo("updated-bill-indentation-value");
        }
    }

    @Nested
    @DisplayName("Custom Query Methods")
    class CustomQueryTests {

        @Test
        @DisplayName("Should find ServerBill by its embedded cryptographic UUID")
        void findByServerBillDesc_Uuid_shouldReturnMatchingBill() {
            // Given: Save the bill to the database
            serverBillsRepository.saveAndFlush(testBill);
            entityManager.clear();

            // When: Execute the custom finder method using java.util.UUID
            Optional<BaseServerBill> found = serverBillsRepository.findByServerBillDesc_Uuid(testBillUuid);

            // Then: Verify it returns the correct matching entity
            assertThat(found).isPresent();
            assertThat(found.get().getServerBillDesc().getUuid()).isEqualTo(testBillUuid);
            assertThat(found.get().getServerBillDesc().getOwnerId()).isEqualTo("test-owner-id-string-value");
        }

        @Test
        @DisplayName("Should return empty Optional when searching for non-existent embedded UUID")
        void findByServerBillDesc_Uuid_shouldReturnEmptyForUnknownUuid() {
            // When: Search for a random UUID that was never persisted
            Optional<BaseServerBill> found = serverBillsRepository.findByServerBillDesc_Uuid(UUID.randomUUID());

            // Then: Verify that it safely returns an empty Optional
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Validation and Null Constraints")
    class ConstraintTests {

        @Test
        @DisplayName("Should throw ConstraintViolationException when indentation length exceeds 256 characters")
        void indentationTooLong_shouldFailValidation() {
            // Given: Indentation string exceeding @Size(max = 256) constraint
            String longIndentation = "a".repeat(257);
            testBill.setIndentation(longIndentation);

            // Then: Expect pre-persist Hibernate validation to fail
            assertThatThrownBy(() -> serverBillsRepository.saveAndFlush(testBill))
                    .isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("Should throw exception when mandatory indentation field is null")
        void nullIndentation_shouldFailValidation() {
            // Given
            testBill.setIndentation(null);

            // Then
            assertThatThrownBy(() -> serverBillsRepository.saveAndFlush(testBill));
        }

        @Test
        @DisplayName("Should throw exception when mandatory billSignature field is null")
        void nullBillSignature_shouldFailValidation() {
            // Given
            testBill.setBillSignature(null);

            // Then
            assertThatThrownBy(() -> serverBillsRepository.saveAndFlush(testBill));
        }
    }

    /**
     * Factory method to construct a valid pre-populated BaseServerBill with required properties.
     * All properties match @NotNull constraints and length definitions from the entity model.
     *
     * @param uuid The unique java.util.UUID identifier inside ServerBillDesc.
     * @return Fully structured BaseServerBill instance.
     */
    private BaseServerBill createTestBill(UUID uuid) {
        BaseServerBill bill = new BaseServerBill();
        bill.setIndentation("test-bill-indentation");
        bill.setBillSignature(new byte[]{10, 20, 30});

        ServerBillDesc desc = new ServerBillDesc();
        desc.setUuid(uuid);
        desc.setOwnerId("test-owner-id-string-value");

        // Cryptographic keys setup matching @NotNull and length = 4096 constraints
        desc.setOwnerPublicKey(new byte[]{1, 2, 3});
        desc.setPublicKey(new byte[]{4, 5, 6});
        desc.setPrivateKey(new byte[]{7, 8, 9});

        bill.setServerBillDesc(desc);
        return bill;
    }
}