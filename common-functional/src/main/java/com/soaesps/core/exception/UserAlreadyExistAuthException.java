package com.soaesps.core.exception;

import org.springframework.security.core.AuthenticationException;

public class UserAlreadyExistAuthException extends AuthenticationException {
    public UserAlreadyExistAuthException(String msg) {
        super(msg);
    }

    public UserAlreadyExistAuthException(String msg, Throwable t) {
        super(msg, t);
    }
}