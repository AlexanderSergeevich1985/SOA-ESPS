package com.soaesps.notifications.service.push;

import com.soaesps.notifications.notifications.DeviceToken;
import com.soaesps.notifications.repository.DeviceTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceTokenServiceTest {

    @Mock
    private DeviceTokenRepository repository;

    @InjectMocks
    private DeviceTokenService service;

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("New token -> entity is created with all fields populated and saved")
        void register_newToken_createsAndSaves() {
            when(repository.findByUserIdAndToken(1L, "fcm-token-1")).thenReturn(Optional.empty());

            service.register(1L, "fcm-token-1", "android");

            ArgumentCaptor<DeviceToken> captor = ArgumentCaptor.forClass(DeviceToken.class);
            verify(repository).save(captor.capture());

            DeviceToken saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(1L);
            assertThat(saved.getToken()).isEqualTo("fcm-token-1");
            assertThat(saved.getPlatform()).isEqualTo("android");
            assertThat(saved.getRegisteredAt()).isNotNull();
            assertThat(saved.getLastSeenAt()).isNotNull();
        }

        @Test
        @DisplayName("Existing token -> updates only lastSeenAt, saves the exact same instance")
        void register_existingToken_updatesLastSeen() {
            Instant registeredAt = Instant.now().minus(10, ChronoUnit.DAYS);
            Instant oldSeen = Instant.now().minus(1, ChronoUnit.DAYS);

            DeviceToken existing = new DeviceToken();
            existing.setUserId(1L);
            existing.setToken("fcm-token-1");
            existing.setPlatform("android");
            existing.setRegisteredAt(registeredAt);
            existing.setLastSeenAt(oldSeen);

            when(repository.findByUserIdAndToken(1L, "fcm-token-1")).thenReturn(Optional.of(existing));

            // Platform is different ('ios') but it should NOT be overwritten
            service.register(1L, "fcm-token-1", "ios");

            verify(repository).save(same(existing));
            assertThat(existing.getLastSeenAt()).isAfter(oldSeen);
            assertThat(existing.getRegisteredAt()).isEqualTo(registeredAt);
            assertThat(existing.getPlatform()).isEqualTo("android");
        }
    }

    @Nested
    @DisplayName("unregister()")
    class UnregisterTests {

        @Test
        @DisplayName("Valid token -> deleteByToken is invoked")
        void unregister_validToken_deletes() {
            service.unregister("fcm-token-1");

            verify(repository).deleteByToken("fcm-token-1");
        }

        @Test
        @DisplayName("Null or blank token -> repository is not interacted with")
        void unregister_nullOrBlank_noInteraction() {
            service.unregister(null);
            service.unregister("   ");

            verifyNoInteractions(repository);
        }
    }

    @Nested
    @DisplayName("tokensFor()")
    class TokensForTests {

        @Test
        @DisplayName("Returns user tokens from the repository")
        void tokensFor_returnsTokens() {
            when(repository.findTokensByUserId(7L)).thenReturn(List.of("t1", "t2"));

            assertThat(service.tokensFor(7L)).containsExactly("t1", "t2");
        }

        @Test
        @DisplayName("Null userId -> returns empty list without invoking repository")
        void tokensFor_nullUserId_emptyList() {
            assertThat(service.tokensFor(null)).isEmpty();

            verifyNoInteractions(repository);
        }
    }
}