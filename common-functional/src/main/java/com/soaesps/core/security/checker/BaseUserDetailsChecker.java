package com.soaesps.core.security.checker;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.stereotype.Component;

@Component
public class BaseUserDetailsChecker implements UserDetailsChecker {
    public void check(UserDetails toCheck) {

    }
}