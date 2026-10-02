package com.soaesps.jetdag.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity // Enables WebFlux-specific reactive security infrastructure
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                // Disable CSRF since our microservice is completely stateless and relies on JWT tokens
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Explicitly lock down our execution engine route
                        .pathMatchers("/api/v1/dag/run/**").authenticated()
                        // Allow actuator health checks or public endpoints if needed
                        .anyExchange().permitAll()
                )
                // Enable JWT validation using standard Spring Security reactive decoders
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }
}