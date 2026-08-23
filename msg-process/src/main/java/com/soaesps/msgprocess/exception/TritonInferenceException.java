package com.soaesps.msgprocess.exception;

public class TritonInferenceException extends RuntimeException {
    public TritonInferenceException(int status, String body) {
        super("Triton returned HTTP " + status + ": " + body);
    }
}