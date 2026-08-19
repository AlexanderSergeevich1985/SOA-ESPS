package com.soaesps.notifications.service.resolver;

import com.soaesps.notifications.notifications.NotificationRecipient;
import com.soaesps.notifications.service.push.DeviceTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RecipientResolver {

    private static final Logger log = LoggerFactory.getLogger(RecipientResolver.class);

    private final DeviceTokenService deviceTokenService;

    /**
     * Dependency injection via constructor.
     * Removed UserProfileClient because all user data is now passed directly in the request.
     */
    public RecipientResolver(DeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    /**
     * Resolves recipient info by combining input user details with active device push tokens.
     * All user meta-data is extracted directly from the incoming request payload.
     *
     * @param userId         The unique identifier of the user.
     * @param phone          User's phone number passed in the event/request.
     * @param email          User's email address passed in the event/request.
     * @param telegramChatId User's Telegram chat identifier passed in the event/request.
     * @return Fully populated NotificationRecipient metadata carrier.
     */
    public NotificationRecipient resolve(Long userId, String phone, String email, String telegramChatId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        log.debug("Resolving notification recipient for user ID: {}, direct data provided", userId);

        // Fetch active device FCM tokens from our own notification database
        List<String> tokens = deviceTokenService.tokensFor(userId);

        // Build the recipient using data directly from the arguments
        return new NotificationRecipient(
                userId,
                phone,
                email,
                telegramChatId,
                tokens.stream().findFirst().get()
        );
    }
}
