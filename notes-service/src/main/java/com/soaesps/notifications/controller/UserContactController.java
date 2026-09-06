package com.soaesps.notifications.controller;

import com.soaesps.notifications.dto.ContactRegistrationRequest;
import com.soaesps.notifications.dto.ContactResponseDto;
import com.soaesps.notifications.dto.UserContactsProfileSummary;
import com.soaesps.notifications.service.UserContactService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Consolidated Tomcat-compatible REST controller managing all communication endpoints.
 * Bridges traditional synchronous servlet worker threads with non-blocking reactive service layers via controlled blocking.
 */
@RestController
@RequestMapping("/api/v1/contacts")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class UserContactController {
    private final UserContactService userContactService;

    public UserContactController(UserContactService userContactService) {
        this.userContactService = userContactService;
    }

    /**
     * Secures and registers a new multi-channel contact destination endpoint.
     * Evaluates the non-blocking service Mono stream inside Tomcat execution boundaries.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponseDto createContact(@AuthenticationPrincipal String authenticatedUserId,
                                            @RequestBody ContactRegistrationRequest request) {
        Long cleanUserId = Long.parseLong(authenticatedUserId);

        // Bridge: Safely execute the reactive pipeline and block the dedicated Tomcat HTTP worker thread
        return userContactService.processContactRegistration(cleanUserId, request)
                .block(); // Safe to block inside servlet-driven thread boundaries
    }

    /**
     * Fetches a consolidated profile summary tracking all active endpoints for the authenticated user.
     */
    @GetMapping
    public UserContactsProfileSummary getMyContactsProfile(@AuthenticationPrincipal String authenticatedUserId) {
        Long cleanUserId = Long.parseLong(authenticatedUserId);

        return userContactService.fetchUserProfileSummary(cleanUserId)
                .block();
    }

    /**
     * Mutates the active state (mute/unmute status coordinates) for a specific user contact record.
     */
    @PutMapping("/{type}/{id}")
    public ContactResponseDto toggleContactActiveState(@AuthenticationPrincipal String authenticatedUserId,
                                                       @PathVariable String type,
                                                       @PathVariable Long id,
                                                       @RequestParam boolean active) {
        Long cleanUserId = Long.parseLong(authenticatedUserId);

        return userContactService.updateContactStatus(cleanUserId, type, id, active)
                .block();
    }

    /**
     * Permanently purges a specific communication channel destination endpoint row.
     */
    @DeleteMapping("/{type}/{id}")
    public ContactResponseDto deleteContact(@AuthenticationPrincipal String authenticatedUserId,
                                            @PathVariable String type,
                                            @PathVariable Long id) {
        Long cleanUserId = Long.parseLong(authenticatedUserId);

        return userContactService.deleteUserContact(cleanUserId, type, id)
                .block();
    }
}