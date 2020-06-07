package com.soaesps.core.security.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.core.Utils.DataStructure.CacheI;
import com.soaesps.core.component.router.TransportI;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.Assert;

public class BaseOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    public static String DEFAULT_AUTH_URL = "";

    private final CacheI<String, OAuth2User> oAuth2Cache;

    private final TransportI sender;

    private final ObjectMapper mapper;

    public BaseOAuth2UserService(final TransportI sender,
                                 final CacheI<String, OAuth2User> oAuth2Cache,
                                 final ObjectMapper mapper) {
        this.oAuth2Cache = oAuth2Cache;
        this.sender = sender;
        this.mapper = mapper;
    }

    @Override
    public OAuth2User loadUser(final OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        Assert.isTrue(isValidOAuth2UserRequest(userRequest), "invalid userRequest");
        OAuth2User oAuth2User = oAuth2Cache.get(userRequest.getClientRegistration().getClientName());
        try {
            if (oAuth2User == null) {
                oAuth2User = sender.sendAndGet(DEFAULT_AUTH_URL, userRequest);
                if (oAuth2User == null) {
                    OAuth2Error oauth2Error = new OAuth2Error("missing_user_name_attribute",
                            "Is impossible to validate oAuth2UserRequest: " + mapper.writeValueAsString(userRequest), (String) null);
                    throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
                }
            }
        } catch (final JsonProcessingException ex) {}

        oAuth2Cache.updateValue(userRequest.getClientRegistration().getClientName(), oAuth2User);

        return oAuth2User;
    }

    public boolean isValidOAuth2UserRequest(final OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        Assert.notNull(userRequest, "userRequest cannot be null");

        return true;
    }
}