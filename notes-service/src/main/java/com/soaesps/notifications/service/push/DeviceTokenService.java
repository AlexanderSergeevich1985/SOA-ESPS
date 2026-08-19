package com.soaesps.notifications.service.push;

import com.soaesps.notifications.notifications.DeviceToken;
import com.soaesps.notifications.repository.DeviceTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DeviceTokenService {

    private final DeviceTokenRepository repository;

    public DeviceTokenService(DeviceTokenRepository repository) {
        this.repository = repository;
    }

    /**
     * Registers a device token or updates its 'lastSeenAt' timestamp if it already exists.
     * This idempotent upsert preventing UniqueConstraintViolationException.
     */
    @Transactional
    public void register(Long userId, String token, String platform) {
        DeviceToken device = repository.findByUserIdAndToken(userId, token)
                .orElseGet(() -> {
                    DeviceToken newDevice = new DeviceToken();
                    newDevice.setUserId(userId);
                    newDevice.setToken(token);
                    newDevice.setPlatform(platform);
                    newDevice.setRegisteredAt(Instant.now());
                    return newDevice;
                });

        device.setLastSeenAt(Instant.now());
        repository.save(device);
    }

    /**
     * Unregisters (deletes) a token from the database.
     * Usually invoked when a user logs out or FCM indicates the token is UNREGISTERED (dead).
     */
    @Transactional
    public void unregister(String token) {
        if (token != null && !token.isBlank()) {
            repository.deleteByToken(token);
        }
    }

    /**
     * Fetches only the active FCM token strings for a specific user.
     */
    public List<String> tokensFor(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return repository.findTokensByUserId(userId);
    }
}