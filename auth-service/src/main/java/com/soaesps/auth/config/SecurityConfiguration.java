package com.soaesps.auth.config;

import com.soaesps.auth.repository.OAuth2TokenRepository;
import com.soaesps.auth.service.security.AccessTokenFactory;
import com.soaesps.auth.service.security.handler.CustomAuthenticationFailureHandler;
import com.soaesps.auth.service.security.handler.CustomAuthenticationSuccessHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ComponentScan("com.soaesps.auth")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Autowired
    private OAuth2TokenRepository tokenRepository;

    @Autowired
    @Qualifier("baseUserDetailsServiceImpl")
    private UserDetailsService userDetailsService;

    @Autowired
    @Qualifier("customAuthenticationProvider")
    private AuthenticationProvider provider;

    @Autowired
    private CustomAuthenticationFailureHandler failureHandler;

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Bean("passwordEncoder")
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccessTokenFactory tokenProvider() {
        return AccessTokenFactory.getInstance(tokenRepository, "secret", 10000);
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(provider)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/").permitAll()
                .anyRequest().authenticated().and()
                .formLogin()
                .loginPage("/login") //login page path
                .loginProcessingUrl("/login_security_check") //login checking path
                .failureUrl("/login?error") //path for login failure
                .failureHandler(failureHandler)
                .successHandler(successHandler)
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .permitAll();
    }
}