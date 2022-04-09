package com.soaesps.core.exception;

public class ExceptionMsg {
    public static final String INVALID_TOKEN_MSG = "";

    public static final String USER_NOT_FOUND_MESSAGE = "";

    public static final String USER_ALREADY_EXIST_MESSAGE = "";

    public static String getUserNotFoundMsg(String userName) {
        return USER_NOT_FOUND_MESSAGE;
    }

    public static String getUserAlreadyExistMsg(String userName) {
        return USER_ALREADY_EXIST_MESSAGE;
    }
}