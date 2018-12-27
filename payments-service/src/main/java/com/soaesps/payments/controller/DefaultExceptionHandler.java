package com.soaesps.payments.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public @ResponseBody ResponseEntity<?> handleException(final Exception ex) {
        if(logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "Exception occurred: ", ex);
        }
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}