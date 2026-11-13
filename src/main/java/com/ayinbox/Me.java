package com.ayinbox;

/** {@code GET /me}. */
public final class Me {
    private final AyInbox client;

    Me(AyInbox client) { this.client = client; }

    /** Validates the credential and returns who it acts for. Needs no scope. */
    public Identity get() { return get(RequestOptions.NONE); }

    public Identity get(RequestOptions options) {
        return client.get("/me", null, options, Identity.class);
    }
}
