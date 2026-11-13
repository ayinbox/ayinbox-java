package com.ayinbox;

/** No response arrived: connection, DNS, TLS failure or timeout. */
public class NetworkException extends AyInboxException {
    private static final long serialVersionUID = 1L;

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
