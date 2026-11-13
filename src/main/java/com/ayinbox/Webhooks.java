package com.ayinbox;

/** Webhook helpers that run locally. Manage endpoints through {@link AyInbox#request}. */
public final class Webhooks {
    Webhooks() {}

    /** See {@link WebhookSignature#verify(byte[], String, String)}. */
    public boolean verifySignature(byte[] payload, String header, String secret) {
        return WebhookSignature.verify(payload, header, secret);
    }

    /** See {@link WebhookSignature#constructEvent(byte[], String, String)}. */
    public WebhookEvent constructEvent(byte[] payload, String header, String secret) {
        return WebhookSignature.constructEvent(payload, header, secret);
    }
}
