package com.soaesps.auth.service.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
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

    private final ObjectMapper mapper;
    private final OAuth2TokenGenerator<?> tokenGenerator;
    private final RegisteredClientRepository clientRepository;
    private final OAuth2AuthorizationService authorizationService;

    public CustomAuthenticationSuccessHandler(ObjectMapper mapper,
                                              @Lazy OAuth2TokenGenerator<?> tokenGenerator,
                                              @Lazy RegisteredClientRepository clientRepository,
                                              @Lazy OAuth2AuthorizationService authorizationService) {
        this.mapper = mapper;
        this.tokenGenerator = tokenGenerator;
        this.clientRepository = clientRepository;
        this.authorizationService = authorizationService;
    }

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
            RegisteredClient registeredClient = clientRepository.findByClientId("browser");
            if (registeredClient == null) {
                throw new IllegalStateException("OAuth2 client 'browser' must be registered in AuthApplication.");
            }

            // Step 3.1: Build standard token contexts required by modern Spring Security
            OAuth2TokenContext accessTokenContext = DefaultOAuth2TokenContext.builder()
                    .registeredClient(registeredClient)
                    .principal(authentication)
                    //.tokenType(org.springframework.security.oauth2.server.authorization.token.OAuth2TokenType.ACCESS_TOKEN)
                    .authorizationGrantType(AuthorizationGrantType.PASSWORD)
                    .build();

            OAuth2TokenContext refreshTokenContext = DefaultOAuth2TokenContext.builder()
                    .registeredClient(registeredClient)
                    .principal(authentication)
                    //.tokenType(org.springframework.security.oauth2.server.authorization.token.OAuth2TokenType.REFRESH_TOKEN)
                    .authorizationGrantType(AuthorizationGrantType.PASSWORD)
                    .build();

            // Step 3.2: Issue cryptographically safe token entities using standard generation pipeline
            OAuth2AccessToken accessToken = (OAuth2AccessToken) tokenGenerator.generate(accessTokenContext);
            OAuth2RefreshToken refreshToken = (OAuth2RefreshToken) tokenGenerator.generate(refreshTokenContext);

            // Step 3.3: Register and persist the issued authorization tokens into the server database context
            OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                    .principalName(authentication.getName())
                    .authorizationGrantType(AuthorizationGrantType.PASSWORD);

            if (accessToken != null) {
                authorizationBuilder.token(accessToken, (metadata) -> metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, accessToken.getTokenType().getValue()));
            }
            if (refreshToken != null) {
                authorizationBuilder.token(refreshToken);
            }
            authorizationService.save(authorizationBuilder.build());

            // Step 3.4: Construct the final payload for the frontend client mapping
            final Map<String, Object> tokenMap = new HashMap<>();
            tokenMap.put("access_token", accessToken != null ? accessToken.getTokenValue() : "");
            tokenMap.put("refresh_token", refreshToken != null ? refreshToken.getTokenValue() : "");
            tokenMap.put("token_type", "Bearer");
            tokenMap.put("expires_in", accessToken != null && accessToken.getExpiresAt() != null ?
                    java.time.Duration.between(java.time.Instant.now(), accessToken.getExpiresAt()).getSeconds() : 3600);

            mapper.writeValue(response.getWriter(), tokenMap);

            final HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            }
        }
    }
}