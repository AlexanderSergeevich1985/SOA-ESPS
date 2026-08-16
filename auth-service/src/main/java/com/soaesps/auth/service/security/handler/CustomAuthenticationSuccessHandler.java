package com.soaesps.auth.service.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.auth.service.security.AccessTokenFactory;
import com.soaesps.core.DataModels.security.BaseOAuth2AccessToken;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component("successHandler")
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private AccessTokenFactory tokenProvider;

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
                                        final Authentication authentication) throws IOException {
        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Step 1: Check if the custom user entity has MFA/2FA enabled globally
        boolean isMfaEnabled = false;
        if (userDetails instanceof com.soaesps.core.DataModels.security.BaseUserDetails) {
            isMfaEnabled = ((com.soaesps.core.DataModels.security.BaseUserDetails) userDetails).isMfaEnabled();
        }

        if (isMfaEnabled) {
            // Step 2: Handle 2FA intercept flow. Generate a temporary handshake session token.
            final String tempToken = UUID.randomUUID().toString();
            final HttpSession session = request.getSession(true);

            // Store the primary verified authentication and user details inside the HTTP session securely
            session.setAttribute("MFA_PRE_AUTH", authentication);
            session.setAttribute("MFA_TEMP_TOKEN", tempToken);

            // Respond with an intermediate JSON instructing frontend to prompt for OTP code
            final Map<String, Object> mfaResponse = new HashMap<>();
            mfaResponse.put("mfaRequired", true);
            mfaResponse.put("tempToken", tempToken);

            mapper.writeValue(response.getWriter(), mfaResponse);
        } else {
            // Step 3: Standard legacy password/certificate flow. Issue final tokens immediately.
            final BaseOAuth2AccessToken accessToken = tokenProvider.createAccessToken(userDetails);

            final Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("accessToken", mapper.writeValueAsString(accessToken));
            tokenMap.put("refreshToken", mapper.writeValueAsString(accessToken));

            mapper.writeValue(response.getWriter(), tokenMap);

            final HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            }
        }
    }
}