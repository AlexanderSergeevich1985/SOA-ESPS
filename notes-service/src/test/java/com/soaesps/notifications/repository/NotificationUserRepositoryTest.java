package com.soaesps.notifications.repository;

import com.soaesps.notifications.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.Hibernate.initialize;

/**
 * Comprehensive integration tests for {@link NotificationUserRepository}.
 * Implements the exact nested structure and assertion styles used in core test suites.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Notification User Repository Integration Tests")
class NotificationUserRepositoryTest extends BaseNotificationRepositoryTest {

    @Autowired
    private NotificationUserRepository notificationUserRepository;

    @Autowired
    private TestEntityManager entityManager;

    private NotificationUser testUser;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
    }

    @Nested
    @DisplayName("Context Loading")
    class ContextTests {

        @Test
        @DisplayName("Repository bean should be successfully injected")
        void repositoryBeanIsAvailable() {
            assertThat(notificationUserRepository).isNotNull();
        }
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class CrudTests {

        @Test
        @DisplayName("Should successfully save, retrieve, and delete a NotificationUser along with nested contacts")
        void saveRetrieveDeleteCycle() {
            // When: Save aggregate root and its single-table polymorphic graph
            NotificationUser saved = notificationUserRepository.saveAndFlush(testUser);
            assertThat(saved.getUserId()).isNotNull();
            Long userId = saved.getUserId();

            entityManager.clear();

            // Then: Retrieve and assert graph loading correctness via explicit JOIN FETCH to avoid lazy exceptions
            NotificationUser retrievedUser = entityManager.getEntityManager().createQuery(
                            "select u from NotificationUser u left join fetch u.contacts where u.userId = :id", NotificationUser.class)
                    .setParameter("id", userId)
                    .getSingleResult();

            assertThat(retrievedUser).isNotNull();
            assertThat(retrievedUser.getDisabledChannels()).containsExactly("SMS");

            List<UserContact> contacts = retrievedUser.getContacts();
            assertThat(contacts).hasSize(2);

            // Polymorphic assertions ensuring classes inside SINGLE_TABLE are parsed properly
            assertThat(contacts)
                    .anySatisfy(c -> {
                        assertThat(c).isInstanceOf(EmailContact.class);
                        assertThat(((EmailContact) c).getEmailAddress()).isEqualTo("test@soaesps.com");
                    })
                    .anySatisfy(c -> {
                        assertThat(c).isInstanceOf(TelegramContact.class);
                        assertThat(((TelegramContact) c).getTelegramChatId()).isEqualTo("tg-1234");
                    });

            // When: Delete root entity (triggers CascadeType.ALL on polymorphic entities)
            notificationUserRepository.delete(retrievedUser);
            entityManager.clear();

            // Then: Verify complete deletion
            assertThat(notificationUserRepository.findById(userId)).isEmpty();
        }

        @Test
        @DisplayName("Should update existing NotificationUser configurations and preferences")
        void updateExistingUser() {
            // Given
            NotificationUser saved = notificationUserRepository.saveAndFlush(testUser);
            entityManager.clear();

            // When
            NotificationUser toUpdate = notificationUserRepository.findById(saved.getUserId()).orElseThrow();
            toUpdate.getDisabledChannels().clear();
            toUpdate.getDisabledChannels().add("EMAIL");
            notificationUserRepository.saveAndFlush(toUpdate);
            entityManager.clear();

            // Then
            NotificationUser updated = notificationUserRepository.findById(saved.getUserId()).orElseThrow();
            assertThat(updated.getDisabledChannels()).containsExactly("EMAIL");
        }
    }

    @Nested
    @DisplayName("Validation and Constraints")
    class ConstraintTests {

        @Test
        @DisplayName("Should throw exception when user disabled channel code exceeds max length")
        void disabledChannelCodeTooLong_shouldFailValidation() {
            // Given: Assuming database or element collection constraint length rules apply (e.g., max 32 chars)
            String invalidChannelCode = "A".repeat(15);
            testUser.getDisabledChannels().clear();
            testUser.getDisabledChannels().add(invalidChannelCode);

            // Then
            assertThatThrownBy(() -> notificationUserRepository.saveAndFlush(testUser));
        }

        @Test
        @DisplayName("Should throw exception when required fields are missing inside the collection mapping")
        void corruptPolymorphicContactProperties_shouldFailPersistence() {
            // Given: Adding a row with null email value to check entity constraints
            EmailContact corruptEmail = new EmailContact();
            corruptEmail.setEmailAddress(null); // Assuming @Column(nullable = false) or validation is active
            testUser.addContact(corruptEmail);

            // Then
            assertThatThrownBy(() -> notificationUserRepository.saveAndFlush(testUser));
        }
    }

    /**
     * Factory method to construct a valid pre-populated NotificationUser aggregate
     * complete with explicit channel settings and polymorphic endpoints.
     */
    private NotificationUser createTestUser() {
        NotificationUser user = new NotificationUser();
        // Mimicking business requirements for explicit IDs from external services
        user.setUserId(9999L);
        user.setDisabledChannels(new ArrayList<>(List.of("SMS")));

        EmailContact email = new EmailContact();
        email.setEmailAddress("test@soaesps.com");
        email.setActive(true);
        email.setPrimary(true);
        user.addContact(email);

        TelegramContact telegram = new TelegramContact();
        telegram.setTelegramChatId("tg-1234");
        telegram.setUsername("@test_user");
        telegram.setActive(true);
        user.addContact(telegram);

        return user;
    }
}