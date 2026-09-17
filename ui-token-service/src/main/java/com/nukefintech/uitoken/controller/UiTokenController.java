package com.nukefintech.uitoken.controller;

import com.nukefintech.uitoken.domain.UiTokenLayout;
import com.nukefintech.uitoken.service.UiTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Ultra-performance Edge API endpoint for Server-Driven UI (SDUI) tokens routing.
 * Serves lightweight JSON design configurations completely out of memory.
 */
@RestController
@RequestMapping("/api/v1/ui-tokens")
public class UiTokenController {

    private static final Logger log = LoggerFactory.getLogger(UiTokenController.class);
    private final UiTokenService uiTokenService;

    public UiTokenController(UiTokenService uiTokenService) {
        this.uiTokenService = uiTokenService;
    }

    /**
     * Resolves the customized look-and-feel style design tokens package
     * based on explicit client view mode selections and device profiling attributes.
     */
    @GetMapping("/layout/{documentType}")
    public Mono<UiTokenLayout> getCustomizedLayoutTokens(
            @AuthenticationPrincipal String userId,
            @PathVariable String documentType,
            @RequestParam(defaultValue = "DEFAULT") String viewMode,
            @RequestParam(defaultValue = "COMPACT") String density) {

        log.debug("[UI-Token-Service] User {} requested design tokens for matrix: {} (Mode: {}, Density: {})",
                userId, documentType, viewMode, density);

        // Streams atomic layout directly from hot Caffeine memory partitions safely
        return uiTokenService.resolveLayoutTokens(documentType, viewMode, density);
    }
}