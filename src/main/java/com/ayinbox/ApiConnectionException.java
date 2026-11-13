package com.ayinbox;

/** The connection failed before a response arrived. */
public class ApiConnectionException extends NetworkException {
    private static final long serialVersionUID = 1L;

    public ApiConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
