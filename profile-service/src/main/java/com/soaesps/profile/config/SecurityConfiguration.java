package com.soaesps.profile.config;

import com.soaesps.core.component.security.CustomAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Security configuration for the profile-service.
 * Configures OAuth2 Login, stateless session management, and method-level security.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // FIXED: Replaces deprecated @EnableGlobalMethodSecurity
public class SecurityConfiguration {

    private final UserDetailsService userDetailsService;
    private final DefaultOAuth2UserService defaultOAuth2UserService;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final AuthenticationFailureHandler authenticationFailureHandler;
    private final AuthorizationRequestRepository authorizationRequestRepository;

    // FIXED: Replaced @Autowired field injection with constructor injection (Spring Boot best practice).
    public SecurityConfiguration(UserDetailsService userDetailsService,
                                 DefaultOAuth2UserService defaultOAuth2UserService,
                                 AuthenticationSuccessHandler authenticationSuccessHandler,
                                 AuthenticationFailureHandler authenticationFailureHandler,
                                 AuthorizationRequestRepository authorizationRequestRepository) {
        this.userDetailsService = userDetailsService;
        this.defaultOAuth2UserService = defaultOAuth2UserService;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.authorizationRequestRepository = authorizationRequestRepository;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Registers the UserDetailsService and PasswordEncoder.
     * Replaces the removed configure(AuthenticationManagerBuilder) method.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Exposes the AuthenticationManager as a bean.
     * Replaces the removed authenticationManagerBean() and BeanIds.AUTHENTICATION_MANAGER pattern.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Main security filter chain configuration using Lambda DSL (Spring Security 6 standard).
     * Replaces the removed WebSecurityConfigurerAdapter and its configure(HttpSecurity) method.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF, FormLogin, and HttpBasic for stateless API / OAuth2 flow
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // Stateless session: no HttpSession created
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Custom 401 Unauthorized handler
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                )

                // Authorization rules
                .authorizeHttpRequests(authorize -> authorize
                        // FIXED: .antMatchers() replaced with .requestMatchers()
                        .requestMatchers("/auth/**", "/oauth2/**", "/actuator/health/**").permitAll()
                        .anyRequest().authenticated()
                )

                // Register custom authentication provider
                .authenticationProvider(authenticationProvider())

                // OAuth2 Login configuration (Lambda DSL)
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorize")
                                .authorizationRequestRepository(authorizationRequestRepository)
                        )
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/oauth2/callback/*")
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(defaultOAuth2UserService)
                        )
                        .successHandler(authenticationSuccessHandler)
                        .failureHandler(authenticationFailureHandler)
                );

        return http.build();
    }

    /**
     * Configures web security to ignore static resources from the security filter chain.
     * Replaces the removed/invalid empty web.ignoring() override.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/static/**", "/css/**", "/js/**", "/images/**", "/favicon.ico"
        );
    }
}