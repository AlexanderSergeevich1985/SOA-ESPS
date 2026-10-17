package com.soaesps.notifications.dto;

import java.util.List;
import java.util.UUID;

/**
 * Comprehensive account profile summary tracking all active channel endpoint mappings.
 */
public record UserContactsProfileSummary(
        UUID userId,
        List<String> activePhoneNumbers,
        List<String> activeEmailAddresses,
        List<String> activeTelegramChats,
        List<String> activePushDeviceTokens
) {}