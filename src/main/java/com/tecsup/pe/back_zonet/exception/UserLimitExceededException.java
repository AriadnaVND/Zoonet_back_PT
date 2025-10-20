package com.tecsup.pe.back_zonet.exception;

public class UserLimitExceededException extends RuntimeException {

    public UserLimitExceededException() {
        super();
    }

    public UserLimitExceededException(String message) {
        super(message);
    }
}
