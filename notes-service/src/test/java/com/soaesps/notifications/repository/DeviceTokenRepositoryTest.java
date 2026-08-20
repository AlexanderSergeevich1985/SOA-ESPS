package com.soaesps.notifications.repository;

import com.soaesps.notifications.notifications.DeviceToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Highly isolated integration test for {@link DeviceTokenRepository}.
 * Explicitly removes Spring Boot's Mockito listeners to prevent Windows Cyrillic path bugs.
 */
@DataJpaTest
@ContextConfiguration(classes = DeviceTokenRepositoryTest.MiniApplication.class)
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class
})
class DeviceTokenRepositoryTest {

    @Autowired
    private DeviceTokenRepository repository;

    @Configuration
    @EnableAutoConfiguration
    @EnableJpaRepositories(basePackages = "com.soaesps.notifications.repository")
    @EntityScan(basePackages = "com.soaesps.notifications")
    static class MiniApplication {
    }

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.save(buildDevice(1L, "token-1", "ANDROID"));
        repository.save(buildDevice(1L, "token-2", "IOS"));
        repository.save(buildDevice(2L, "token-3", "WEB"));
        repository.flush();
    }

    @Test
    @DisplayName("findByUserIdAndToken should return the matching device")
    void findByUserIdAndToken_shouldFindExistingDevice() {
        Optional<DeviceToken> found = repository.findByUserIdAndToken(1L, "token-1");

        assertThat(found).isPresent();
        assertThat(found.get().getPlatform()).isEqualTo("ANDROID");
    }

    @Test
    @DisplayName("findByUserIdAndToken should return empty for unknown combination")
    void findByUserIdAndToken_shouldReturnEmptyForUnknownPair() {
        assertThat(repository.findByUserIdAndToken(1L, "token-3")).isEmpty();
        assertThat(repository.findByUserIdAndToken(99L, "token-1")).isEmpty();
    }

    @Test
    @DisplayName("Unique constraint (user_id, token) should reject duplicate registration")
    void uniqueConstraint_shouldRejectDuplicateUserTokenPair() {
        DeviceToken duplicate = buildDevice(1L, "token-1", "WEB");

        assertThatThrownBy(() -> repository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("The same token may belong to different users")
    void uniqueConstraint_shouldAllowSameTokenForDifferentUsers() {
        DeviceToken crossUser = buildDevice(1L, "token-3", "ANDROID");

        DeviceToken saved = repository.saveAndFlush(crossUser);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("deleteByToken should remove the token for all its owners")
    void deleteByToken_shouldRemoveToken() {
        repository.deleteByToken("token-1");
        repository.flush();

        assertThat(repository.findByUserIdAndToken(1L, "token-1")).isEmpty();
        assertThat(repository.findByUserIdAndToken(1L, "token-2")).isPresent();
    }

    @Test
    @DisplayName("findTokensByUserId should project only token strings of the given user")
    void findTokensByUserId_shouldReturnOnlyOwnTokens() {
        List<String> tokens = repository.findTokensByUserId(1L);

        assertThat(tokens).containsExactlyInAnyOrder("token-1", "token-2");
    }

    @Test
    @DisplayName("findTokensByUserId should return empty list for user without devices")
    void findTokensByUserId_shouldReturnEmptyListForUnknownUser() {
        List<String> tokens = repository.findTokensByUserId(999L);

        assertThat(tokens).isEmpty();
    }

    private DeviceToken buildDevice(Long userId, String token, String platform) {
        DeviceToken device = new DeviceToken();
        device.setUserId(userId);
        device.setToken(token);
        device.setPlatform(platform);
        device.setRegisteredAt(Instant.now());
        device.setLastSeenAt(Instant.now());
        return device;
    }
}