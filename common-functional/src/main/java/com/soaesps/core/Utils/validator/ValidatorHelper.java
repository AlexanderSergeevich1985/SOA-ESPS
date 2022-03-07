package com.soaesps.core.Utils.validator;

import org.springframework.validation.Validator;

public class ValidatorHelper {
    public static final String DEFAULT_NULL_VALUE_MSG = "value is null";

    public static final String DEFAULT_EMPTY_VALUE_MSG = "value is empty";

    private ValidatorHelper() {
        throw new UnsupportedOperationException();
    }

    public static String commonValidation(String value) {
        if (value == null) {
            return DEFAULT_NULL_VALUE_MSG;
        }
        if (value.trim().isEmpty()) {
            return DEFAULT_EMPTY_VALUE_MSG;
        }

        return null;
    }
}