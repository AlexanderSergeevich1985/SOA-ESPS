package com.soaesps.core.security.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

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