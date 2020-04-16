package com.soaesps.auth.service.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    public final ErrorResponse errorResponse = new ErrorResponse();

    @Autowired
    private ObjectMapper mapper;

    @Override
    public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response,
                                        final AuthenticationException ex) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getWriter(), errorResponse(this, ex));
    }

    public static class ErrorResponse {
        private AuthenticationException exception;

        private LocalDateTime timeStamp;

        public ErrorResponse() {}

        public ErrorResponse(final AuthenticationException ex) {
            this.exception = ex;
        }

        public ErrorResponse setException(final AuthenticationException exception) {
            this.exception = exception;
            this.timeStamp = LocalDateTime.now();

            return this;
        }
    }

    static public ErrorResponse errorResponse(
            final CustomAuthenticationFailureHandler handler,
            final AuthenticationException ex) {
        try {
            return handler.errorResponse.setException(ex);
        } catch (final NullPointerException npe) {
            return new ErrorResponse(ex);
        }
    }
}