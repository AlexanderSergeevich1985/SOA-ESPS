package com.soaesps.core.security.handler;

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
import java.util.HashMap;
import java.util.Map;

public class BaseAuthenticationFailureHandler implements AuthenticationFailureHandler {
    public static final String DEFAULT_MESSAGE = "Access Denied";

    public final ErrorResponse errorResponse = new ErrorResponse();

    @Autowired
    private ObjectMapper mapper;

    @Override
    public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response,
                                        final AuthenticationException ex) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        final Map<String, Object> data = new HashMap<>();
        data.put("error", errorResponse(this, ex));
        data.put("message", DEFAULT_MESSAGE);
        data.put("path", request.getRequestURL().toString());
        mapper.writeValue(response.getWriter(), errorResponse(this, ex));
    }

    public static class ErrorResponse {
        private String url;

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

    private static ErrorResponse errorResponse(final BaseAuthenticationFailureHandler handler,
                                               final AuthenticationException ex) {
        try {
            return handler.errorResponse.setException(ex);
        } catch (final NullPointerException npe) {
            return new ErrorResponse(ex);
        }
    }
}