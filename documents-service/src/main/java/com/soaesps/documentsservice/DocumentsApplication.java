package com.soaesps.documentsservice;

import feign.RequestInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients; // FIXED: Modern OpenFeign import
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Main application class for the documents-service microservice.
 * Configures OAuth2 Resource Server with JWT validation using WebFlux security model.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.soaesps.documentsservice")
@EnableWebFluxSecurity
@EnableMethodSecurity
public class DocumentsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentsApplication.class, args);
    }

    /**
     * Reactive security filter chain configuration.
     * Replaces the removed ResourceServerConfigurerAdapter.
     * Uses SecurityWebFilterChain (not SecurityFilterChain) for WebFlux applications.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // Disable CSRF for stateless REST API with JWT tokens
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // Authorization rules
                .authorizeExchange(exchanges -> exchanges
                        // FIXED: .antMatchers() replaced with .pathMatchers() for WebFlux
                        .pathMatchers("/documents/**", "/actuator/health/**").permitAll()
                        .anyExchange().authenticated()
                )

                // Enable OAuth2 Resource Server with JWT validation
                // Replaces @EnableResourceServer and ResourceServerConfigurerAdapter
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {})
                )
                .build();
    }

    /**
     * WebClient configuration for making HTTP requests to other microservices.
     * Replaces the removed OAuth2RestTemplate.
     * Automatically propagates OAuth2 tokens from the security context.
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                // Automatically extracts and forwards the JWT token from SecurityContext
                .filter((request, next) ->
                        ReactiveSecurityContextHolder.getContext()
                                .map(securityContext -> {
                                    Authentication authentication = securityContext.getAuthentication();
                                    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                                        return request.headers(headers ->
                                                headers.setBearerAuth(jwtAuth.getToken().getTokenValue())
                                        );
                                    }
                                    return request;
                                })
                                .defaultIfEmpty(request)
                                .flatMap(next::exchange)
                )
                .build();
    }

    /**
     * Feign interceptor that propagates the incoming JWT token to downstream microservice calls.
     * Replaces the removed OAuth2FeignRequestInterceptor.
     *
     * NOTE: In reactive applications, Feign (which is blocking) may not work well with WebFlux.
     * Consider using WebClient instead of Feign for better reactive compatibility.
     */
    @Bean
    public RequestInterceptor requestTokenBearerInterceptor() {
        return requestTemplate -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                // Internal system call without security context
                return;
            }

            // In Spring Security 6, JWT tokens are wrapped in JwtAuthenticationToken
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                String tokenValue = jwtAuth.getToken().getTokenValue();
                requestTemplate.header("Authorization", "Bearer " + tokenValue);
            }
            // Fallback for other authentication types
            else if (authentication.getCredentials() instanceof String token) {
                requestTemplate.header("Authorization", "Bearer " + token);
            }
        };
    }
}