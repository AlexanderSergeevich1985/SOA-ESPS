package com.soaesps.notifications.dto;

import java.util.List;

/**
 * Comprehensive account profile summary tracking all active channel endpoint mappings.
 */
public record UserContactsProfileSummary(
        Long userId,
        List<String> activePhoneNumbers,
        List<String> activeEmailAddresses,
        List<String> activeTelegramChats,
        List<String> activePushDeviceTokens
) {}