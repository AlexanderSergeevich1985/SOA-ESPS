package com.soaesps.auth.service.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.soaesps.auth.service.security.AccessTokenFactory;
import com.soaesps.core.DataModels.security.BaseOAuth2AccessToken;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Component("successHandler")
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private AccessTokenFactory tokenProvider;

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
                                        final Authentication authentication) throws IOException {
        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        final BaseOAuth2AccessToken accessToken = tokenProvider.createAccessToken(userDetails);

        final Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", mapper.writeValueAsString(accessToken));
        tokenMap.put("refreshToken", mapper.writeValueAsString(accessToken));

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.sendRedirect(request.getRequestURL().toString());
        mapper.writeValue(response.getWriter(), tokenMap);

        final HttpSession session = request.getSession(false);

        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }
}