package com.ayinbox;

/** A webhook signature did not verify. */
public class SignatureVerificationException extends AyInboxException {
    private static final long serialVersionUID = 1L;

    public SignatureVerificationException(String message) {
        super(message);
    }
}
