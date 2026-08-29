package com.soaesps.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.auth.service.security.AccessTokenFactory;
import com.soaesps.auth.service.security.OtpVerificationService;
import com.soaesps.core.DataModels.security.BaseOAuth2AccessToken;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login/otp")
public class MfaRestController {

    @Autowired
    private AccessTokenFactory tokenProvider;

    @Autowired
    private OtpVerificationService otpService;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Validates the submitted OTP token and generates full operational OAuth2 tokens upon success.
     *
     * @param body    the JSON payload container holding "code" and "tempToken" keys
     * @param session the current active stateful HTTP context container
     * @return operational map containing final accessToken and refreshToken sequences
     * @throws Exception if processing token factory bindings fails unexpectedly
     */
    @PostMapping(value = "/verify", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> verifyOtp(@RequestBody final Map<String, String> body, final HttpSession session) throws Exception {
        if (body == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Request body missing"));
        }

        final String code = body.get("code");
        final String tempToken = body.get("tempToken");

        final Authentication preAuth = (Authentication) session.getAttribute("MFA_PRE_AUTH");
        final String savedTempToken = (String) session.getAttribute("MFA_TEMP_TOKEN");

        // Validate state safety constraints and handshake token matching signatures
        if (preAuth == null || savedTempToken == null || !savedTempToken.equals(tempToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Session expired or invalid"));
        }

        // Delegate numerical validation logic checks directly to the core OTP verification provider
        final boolean isValid = otpService.validateCode(preAuth.getName(), code);
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Invalid one-time password code"));
        }

        // Re-establish fully authorized profile identity inside active Spring Security environment scope
        SecurityContextHolder.getContext().setAuthentication(preAuth);

        final UserDetails userDetails = (UserDetails) preAuth.getPrincipal();
        final BaseOAuth2AccessToken accessToken = tokenProvider.createAccessToken(userDetails);

        // Discard short-lived validation metadata from persistent state bounds safely
        session.removeAttribute("MFA_PRE_AUTH");
        session.removeAttribute("MFA_TEMP_TOKEN");

        // Construct standardized REST serialization contract matching legacy response models
        final Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("access_token", accessToken.getValue());
        tokenMap.put("refresh_token", accessToken.getRefreshToken().getValue());
        tokenMap.put("token_type", "Bearer");

        return ResponseEntity.ok(tokenMap);
    }
}