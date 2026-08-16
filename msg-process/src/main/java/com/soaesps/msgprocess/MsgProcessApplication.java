package com.soaesps.msgprocess;

import feign.RequestInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Main application class for the msg-process microservice.
 * Configures OAuth2 Resource Server with JWT validation and Feign client token propagation.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.soaesps.msgprocess") // FIXED: Modern OpenFeign annotation
@EnableWebSecurity
@EnableMethodSecurity // FIXED: Replaces deprecated @EnableGlobalMethodSecurity
public class MsgProcessApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsgProcessApplication.class, args);
    }

    /**
     * Main security filter chain configuration.
     * Replaces the removed ResourceServerConfigurerAdapter.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless REST API with JWT tokens
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless session: no HttpSession created, each request must contain JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Authorization rules
                .authorizeHttpRequests(authorize -> authorize
                        // FIXED: .antMatchers() replaced with .requestMatchers()
                        .requestMatchers("/apps/**").permitAll()
                        .requestMatchers("/actuator/health/**").permitAll()
                        .anyRequest().authenticated()
                )

                // Enable OAuth2 Resource Server with JWT validation
                // Replaces @EnableResourceServer and ResourceServerConfigurerAdapter
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(org.springframework.security.config.Customizer.withDefaults())
                );

        return http.build();
    }

    /**
     * Feign interceptor that propagates the incoming JWT token to downstream microservice calls.
     * Replaces the removed OAuth2FeignRequestInterceptor.
     */
    @Bean
    public RequestInterceptor requestTokenBearerInterceptor() {
        return requestTemplate -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                // Internal system call without security context (e.g., scheduled task)
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