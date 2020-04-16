package com.soaesps.auth.service.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Autowired
    private ObjectMapper mapper;

    @Override
    public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response,
                                        final AuthenticationException ex) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getWriter(), errorResponse(this, ex));
    }

    public class ErrorResponse {
        private final AuthenticationException exception;

        private final LocalDateTime timeStamp = LocalDateTime.now();

        public ErrorResponse(final AuthenticationException ex) {
            this.exception = ex;
        }
    }

    static public ErrorResponse errorResponse(
            final CustomAuthenticationFailureHandler handler,
            final AuthenticationException ex) {
        return handler.new ErrorResponse(ex);
    }
}