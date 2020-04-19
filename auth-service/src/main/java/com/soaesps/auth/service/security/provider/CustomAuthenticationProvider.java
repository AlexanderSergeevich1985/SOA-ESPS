package com.soaesps.auth.service.security.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component("customAuthenticationProvider")
public class CustomAuthenticationProvider implements org.springframework.security.authentication.AuthenticationProvider {
    @Autowired
    @Qualifier("passwordEncoder")
    private PasswordEncoder passwordEncoder;

    @Autowired
    @Qualifier("baseUserDetailsServiceImpl")
    private UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;

        final String name = token.getName();
        final UserDetails user = userDetailsService.loadUserByUsername(name);
        if (user == null) {
            throw new UsernameNotFoundException("[AuthenticationProvider/authenticate]: user not found");
        }

        final String password = token.getCredentials().toString();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("[AuthenticationProvider/authenticate]: invalid credentials");
        }

        return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.equals(authentication);
    }
}