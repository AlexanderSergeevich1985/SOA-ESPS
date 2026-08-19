package com.soaesps.core.security.config;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Maps the CN (Common Name) of a client certificate to a service principal.
 * Example: CN=auth-service -> principal "auth-service"
 * with authorities ROLE_SERVICE and ROLE_AUTH_SERVICE.
 */
public class MtlsUserDetailsService implements UserDetailsService {

    private final String serviceRole;

    public MtlsUserDetailsService(String serviceRole) {
        this.serviceRole = serviceRole;
    }

    @Override
    public User loadUserByUsername(String cn) throws UsernameNotFoundException {
        return new User(
                cn,
                "", // no password — authentication is performed at the TLS layer
                AuthorityUtils.createAuthorityList(
                        "ROLE_" + serviceRole,
                        "ROLE_" + cn.toUpperCase().replace("-", "_")
                )
        );
    }
}