package com.soaesps.auth.config;

import com.soaesps.auth.repository.OAuth2TokenRepository;
import com.soaesps.auth.service.security.AccessTokenFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ComponentScan({"com.soaesps.core.security"})
@Import(com.soaesps.core.config.BaseAuthorizationServerConfiguration.class)
public class SecurityConfiguration {

    @Autowired
    private OAuth2TokenRepository tokenRepository;

    @Autowired
    @Qualifier("baseUserDetailsServiceImpl")
    private UserDetailsService userDetailsService;

    @Autowired
    @Qualifier("customAuthenticationProvider")
    private AuthenticationProvider provider;

    @Autowired
    private AuthenticationFailureHandler failureHandler;

    @Autowired
    private AuthenticationSuccessHandler successHandler;

    @Bean
    public AccessTokenFactory tokenProvider() {
        return AccessTokenFactory.getInstance(tokenRepository, "secret", 10000);
    }

    /**
     * Configures the security filter chain dedicated to human interactive users.
     * Preserves all original custom endpoints, handlers, routing paths, and authentication structures.
     * The primary technical mTLS chain is inherited automatically from BaseAuthorizationServerConfiguration.
     */
    @Bean
    @Order(2) // Выполняется сразу после цепочки OAuth2 сервера авторизации
    public SecurityFilterChain userSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/login", "/login_security_check", "/logout", "/accounts/**", "/login/otp", "/login/otp/verify")
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception
                                .defaultAuthenticationEntryPointFor(
                                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                        new MediaTypeRequestMatcher(MediaType.APPLICATION_JSON)
                                )
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                .authorizeHttpRequests(authorize -> authorize
                        // Added /login/otp and verification endpoints to the open list
                        .requestMatchers("/login/otp", "/login/otp/verify").permitAll()
                        // Allow anonymous access to creation, but let Method Security (@PreAuthorize)
                        // handle internal strict rules (like isAnonymous, hasRole) on Controller level
                        .requestMatchers(HttpMethod.POST, "/accounts/create").permitAll()
                        // Secure all other account operations
                        .requestMatchers("/accounts/**").authenticated()
                        .anyRequest().authenticated()
                )
                // Strategy A for human actors: Extract Username from personal smartcard or client certificate CN field
                .x509(x509 -> x509
                        .subjectPrincipalRegex("CN=(.*?)(?:,|$)")
                        .userDetailsService(userDetailsService)
                )
                // Strategy B for human actors: Legacy fallback form login with your original precise mappings
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .loginProcessingUrl("/login_security_check")
                        .failureHandler(failureHandler)
                        .successHandler(successHandler)
                        .permitAll()
                )
                // Session destruction and invalidation behavior mapping the original configuration block
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .permitAll()
                )
                // Wire up your custom internal identity validation logic provider
                .authenticationProvider(provider);

        return http.build();
    }
}