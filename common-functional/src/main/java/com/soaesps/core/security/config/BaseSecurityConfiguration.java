package com.soaesps.core.security.config;

import feign.RequestInterceptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Standard dual-authentication security configuration for all SOA-ESPS microservices.
 *
 * Simultaneously supports:
 *  - X.509 client certificates (mTLS) for inter-service communication;
 *  - OAuth2 Bearer tokens (JWT) for end-user requests (optional per service).
 *
 * Each service customizes behavior via 'soa.security.*' properties in application.yml.
 * Services that need fully custom rules may define their own SecurityFilterChain bean —
 * in that case this standard chain is skipped (@ConditionalOnMissingBean).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(SoaSecurityProperties.class)
public class BaseSecurityConfiguration {

    @Bean
    public MtlsUserDetailsService mtlsUserDetailsService(SoaSecurityProperties properties) {
        return new MtlsUserDetailsService(properties.getMtls().getServiceRole());
    }

    /**
     * Standard entry point for REST APIs.
     * Returns a clean 401 Unauthorized status instead of a browser basic auth popup.
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public AuthenticationProvider authenticationProvider(MtlsUserDetailsService mtlsUserDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(mtlsUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    @Primary
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public RequestInterceptor requestTokenBearerInterceptor() {
        return requestTemplate -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return;
            }
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                String tokenValue = jwtAuth.getToken().getTokenValue();
                requestTemplate.header("Authorization", "Bearer " + tokenValue);
            }
            else if (authentication.getCredentials() instanceof String token) {
                requestTemplate.header("Authorization", "Bearer " + token);
            }
        };
    }

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(name = "customSecurityFilterChain")
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   SoaSecurityProperties properties,
                                                   MtlsUserDetailsService mtlsUserDetailsService) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 1. mTLS (X.509) authentication for inter-service calls.
        //    Active only when a client certificate is presented during TLS handshake.
        if (properties.getMtls().isEnabled()) {
            http.x509(x509 -> x509
                    .subjectPrincipalRegex("CN=(.*?)(?:,|$)")
                    .userDetailsService(mtlsUserDetailsService));
        }

        // 2. JWT Bearer token authentication for end users (optional per service).
        if (properties.getJwt().isEnabled()) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        }

        // 3. Authorization rules driven by properties.
        http.authorizeHttpRequests(auth -> auth
                // Public endpoints: no auth required
                .requestMatchers(properties.getPermitAll().toArray(String[]::new)).permitAll()
                // Internal endpoints: ONLY services with a trusted client certificate
                .requestMatchers(properties.getInternal().toArray(String[]::new))
                .hasRole(properties.getMtls().getServiceRole())
                // Everything else: any authenticated principal (JWT user or mTLS service)
                .anyRequest().authenticated());

        return http.build();
    }
}