package com.soaesps.auth.service.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.soaesps.core.DataModels.security.BaseUserDetails;

import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
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
        boolean isMfaEnabled = userDetails instanceof BaseUserDetails bud && bud.isMfaEnabled();

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
            RegisteredClient registeredClient = null;
            Authentication clientAuth = SecurityContextHolder.getContext().getAuthentication();

            if (clientAuth instanceof OAuth2ClientAuthenticationToken) {
                registeredClient = ((OAuth2ClientAuthenticationToken) clientAuth).getRegisteredClient();
            } else {
                String clientId = request.getParameter("client_id");
                if (clientId == null) {
                    clientId = request.getHeader("client_id");
                }
                if (clientId != null) {
                    registeredClient = clientRepository.findByClientId(clientId);
                }
            }

            // Fallback logic. If no client was specified (e.g. raw curl request), apply default profile.
            if (registeredClient == null) {
                registeredClient = clientRepository.findByClientId("browser");
            }

            if (registeredClient == null) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error(
                                OAuth2ErrorCodes.INVALID_CLIENT,
                                "Missing or invalid OAuth2 client identifier. The application must provide a valid client_id parameter.",
                                null
                        )
                );
            }

            // Step 3: Fetch metadata parameters from global server deployment architecture
            AuthorizationServerContext serverContext = AuthorizationServerContextHolder.getContext();
            if (serverContext == null) {
                final String issuerUri = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
                serverContext = new AuthorizationServerContext() {
                    @Override
                    public String getIssuer() {
                        return issuerUri;
                    }
                    @Override
                    public AuthorizationServerSettings getAuthorizationServerSettings() {
                        return AuthorizationServerSettings.builder().issuer(issuerUri).build();
                    }
                };
            }

            UsernamePasswordAuthenticationToken userPrincipal =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // Step 4: Configure explicit generation payload boundaries using active client criteria
            OAuth2TokenContext accessTokenContext = DefaultOAuth2TokenContext.builder()
                    .registeredClient(registeredClient)
                    .principal(userPrincipal)
                    .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                    .authorizationServerContext(serverContext)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .build();

            OAuth2TokenContext refreshTokenContext = DefaultOAuth2TokenContext.builder()
                    .registeredClient(registeredClient)
                    .principal(userPrincipal)
                    .tokenType(OAuth2TokenType.REFRESH_TOKEN)
                    .authorizationServerContext(serverContext)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .build();

            OAuth2Token generatedAccessToken = tokenGenerator.generate(accessTokenContext);
            OAuth2Token generatedRefreshToken  = tokenGenerator.generate(refreshTokenContext);

            // Step 6: Map to dedicated OAuth2 token wrapper classes safely avoiding ClassCastException
            OAuth2AccessToken accessToken;
            if (generatedAccessToken instanceof Jwt jwt) {
                accessToken = new OAuth2AccessToken(
                        OAuth2AccessToken.TokenType.BEARER,
                        jwt.getTokenValue(),
                        jwt.getIssuedAt(),
                        jwt.getExpiresAt(),
                        accessTokenContext.getAuthorizedScopes()
                );
            } else if (generatedAccessToken instanceof OAuth2AccessToken token) {
                accessToken = token;
            } else {
                accessToken = null;
            }

            OAuth2RefreshToken refreshToken = generatedRefreshToken instanceof OAuth2RefreshToken token ? token : null;

            // Step 6: Register and persist the issued authorization tokens into the server database context
            OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                    .principalName(authentication.getName())
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);

            if (accessToken != null) {
                authorizationBuilder.token(accessToken, (metadata) ->
                        metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, accessToken.getTokenType().getValue()));
            }
            if (refreshToken != null) {
                authorizationBuilder.token(refreshToken);
            }
            authorizationService.save(authorizationBuilder.build());

            // Step 7: Construct the final payload for the frontend client mapping
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