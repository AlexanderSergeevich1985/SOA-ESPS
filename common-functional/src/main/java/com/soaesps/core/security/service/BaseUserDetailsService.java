package com.soaesps.core.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.core.component.router.TransportI;

import org.springframework.security.core.userdetails.*;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

public class BaseUserDetailsService implements UserDetailsService {
    private static String DEFAULT_AUTH_URL = "";

    private final ObjectMapper mapper;

    private final UserCache userCache;

    private final TransportI sender;
    public BaseUserDetailsService(final TransportI sender,
                                  final UserCache userCache,
                                  final ObjectMapper mapper) {
        this.userCache = userCache;
        this.sender = sender;
        this.mapper = mapper;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UserDetails loadUserByUsername(final String userName) {
        UserDetails userDetails = userCache.getUserFromCache(userName);
        if (userDetails == null) {
            userDetails = sender.sendAndGet(DEFAULT_AUTH_URL, userName);
            if(userDetails == null) {
                throw new UsernameNotFoundException("User with name" + userName + " hasn't registered");
            }
        }

        return userDetails;
    }
}