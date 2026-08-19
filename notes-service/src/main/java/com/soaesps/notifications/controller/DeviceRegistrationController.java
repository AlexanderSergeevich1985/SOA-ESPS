package com.soaesps.notifications.controller;

import com.soaesps.notifications.service.push.DeviceTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints used by the mobile app to register/unregister its FCM token.
 * Protected by JWT (end-user authentication).
 */
@RestController
@RequestMapping("/api/user/devices")
public class DeviceRegistrationController {

    private final DeviceTokenService deviceTokenService;

    public DeviceRegistrationController(DeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    public record DeviceRegistrationRequest(String token, String platform) {}

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody DeviceRegistrationRequest request,
                                         Authentication auth) {
        // auth.getName() -> username; resolve userId via your user service
        deviceTokenService.register(resolveUserId(auth), request.token(), request.platform());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/unregister")
    public ResponseEntity<Void> unregister(@RequestBody DeviceRegistrationRequest request) {
        deviceTokenService.unregister(request.token());
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(Authentication auth) {
        // TODO: map username -> userId via UserDetailsService / Feign to auth-service
        return 0L;
    }
}