package com.soaesps.auth.config;

import com.soaesps.auth.repository.OAuth2TokenRepository;
import com.soaesps.auth.service.security.AccessTokenFactory;
import com.soaesps.auth.service.security.handler.CustomAuthenticationSuccessHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.core.annotation.Order;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ComponentScan({"com.soaesps.auth", "com.soaesps.core.security"})
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
    private CustomAuthenticationSuccessHandler successHandler;

    /*@Bean("passwordEncoder")
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }*/

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
    @Order(2) // Executes immediately after the high-priority technical OAuth2/mTLS filter chain
    public SecurityFilterChain userSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Replaces old .csrf().disable()
                .authorizeHttpRequests(authorize -> authorize
                        // Added /login/otp and verification endpoints to the open list
                        .requestMatchers("/accounts/**", "/login/otp", "/login/otp/verify").permitAll()
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
                        .failureUrl("/login?error")
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

    /*@Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }*/
}