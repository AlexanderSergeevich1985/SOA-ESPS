package com.soaesps.core.exception;

public class ValidatorException extends RuntimeException {
    public ValidatorException() {
        super();
    }

    public ValidatorException(String errMsg) {
        super(errMsg);
    }
}