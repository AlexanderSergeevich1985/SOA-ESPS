package com.soaesps.msgprocess.exception;

/** Thrown for frames that can never be processed — go straight to DLQ, no retry. */
public class MalformedFrameException extends RuntimeException {
    public MalformedFrameException(String message) { super(message); }
    public MalformedFrameException(String message, Throwable cause) { super(message, cause); }
}