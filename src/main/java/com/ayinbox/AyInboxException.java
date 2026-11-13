package com.ayinbox;

/** Base of every exception this library throws. */
public class AyInboxException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AyInboxException(String message) {
        super(message);
    }

    public AyInboxException(String message, Throwable cause) {
        super(message, cause);
    }
}
