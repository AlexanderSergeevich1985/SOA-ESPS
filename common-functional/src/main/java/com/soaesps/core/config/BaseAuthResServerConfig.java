package com.soaesps.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.soaesps.core.Utils.DataStructure.CacheI;
import com.soaesps.core.component.router.TransportI;
import com.soaesps.core.security.handler.BaseAuthenticationFailureHandler;
import com.soaesps.core.security.handler.BaseAuthenticationSuccessHandler;
import com.soaesps.core.security.service.BaseOAuth2UserService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class BaseAuthResServerConfig extends WebSecurityConfigurerAdapter {
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new BaseAuthenticationSuccessHandler();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new BaseAuthenticationFailureHandler();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService(final TransportI sender,
                                                                        final CacheI<String, OAuth2User> oAuth2Cache,
                                                                        final ObjectMapper mapper) {
        return new BaseOAuth2UserService(sender, oAuth2Cache, mapper);
    }

    @Bean
    public AuthorizationRequestRepository customAuthorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }
}