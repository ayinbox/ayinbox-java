package com.ayinbox;

/** The call timed out. */
public class ApiTimeoutException extends NetworkException {
    private static final long serialVersionUID = 1L;

    public ApiTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
