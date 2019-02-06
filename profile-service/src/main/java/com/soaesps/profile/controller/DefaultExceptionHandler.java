package com.soaesps.profile.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.logging.Level;
import java.util.logging.Logger;

@ControllerAdvice
public class DefaultExceptionHandler {
    public static Logger logger;

    static {
        logger = Logger.getLogger(DefaultExceptionHandler.class.getName());
        logger.setLevel(Level.INFO);
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleException(final Exception ex) {
        if(logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "Exception occurred: ", ex);
        }
    }
}