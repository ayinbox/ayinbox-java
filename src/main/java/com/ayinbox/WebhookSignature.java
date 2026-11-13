package com.ayinbox;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Verifies the {@code X-Ayinbox-Signature} header: HMAC-SHA256 keyed with the endpoint secret (prefix included)
 * over {@code "<t>.<raw body>"}, compared in constant time.
 */
public final class WebhookSignature {
    /** Default tolerance between the signed timestamp and now. */
    public static final long DEFAULT_TOLERANCE_SECONDS = 300;

    private WebhookSignature() {}

    /** Verifies against the current time with a 300 second tolerance. */
    public static boolean verify(byte[] payload, String header, String secret) {
        return verify(payload, header, secret, DEFAULT_TOLERANCE_SECONDS, System.currentTimeMillis() / 1000L);
    }

    public static boolean verify(String payload, String header, String secret) {
        return verify(payload.getBytes(StandardCharsets.UTF_8), header, secret);
    }

    /**
     * @param payload          the raw request body, before any JSON parsing
     * @param header           the {@code X-Ayinbox-Signature} value
     * @param secret           the endpoint secret, {@code whsec_} prefix included
     * @param toleranceSeconds must be positive
     * @param nowEpochSeconds  the current time
     */
    public static boolean verify(byte[] payload, String header, String secret, long toleranceSeconds, long nowEpochSeconds) {
        if (payload == null || header == null || secret == null || secret.isEmpty()) return false;
        if (toleranceSeconds <= 0) throw new IllegalArgumentException("toleranceSeconds must be positive");
        Long t = null;
        List<String> sigs = new ArrayList<>();
        for (String part : header.split(",")) {
            int eq = part.indexOf('=');
            if (eq < 0) continue;
            String k = part.substring(0, eq).trim();
            String v = part.substring(eq + 1).trim();
            if ("t".equals(k)) {
                try {
                    t = Long.parseLong(v);
                } catch (NumberFormatException e) {
                    return false;
                }
            } else if ("v1".equals(k)) {
                sigs.add(v);
            }
        }
        if (t == null || sigs.isEmpty()) return false;
        if (Math.abs(nowEpochSeconds - t) > toleranceSeconds) return false;
        byte[] expected = hex(hmac(secret, t, payload)).getBytes(StandardCharsets.US_ASCII);
        boolean ok = false;
        for (String s : sigs) {
            ok |= MessageDigest.isEqual(expected, s.getBytes(StandardCharsets.US_ASCII));
        }
        return ok;
    }

    /** Verifies, then parses the delivery. Throws {@link SignatureVerificationException} when it does not verify. */
    public static WebhookEvent constructEvent(byte[] payload, String header, String secret) {
        return constructEvent(payload, header, secret, DEFAULT_TOLERANCE_SECONDS, System.currentTimeMillis() / 1000L);
    }

    public static WebhookEvent constructEvent(byte[] payload, String header, String secret, long toleranceSeconds, long nowEpochSeconds) {
        if (!verify(payload, header, secret, toleranceSeconds, nowEpochSeconds)) {
            throw new SignatureVerificationException("Webhook signature did not verify");
        }
        try {
            return Json.MAPPER.readValue(payload, WebhookEvent.class);
        } catch (java.io.IOException e) {
            throw new SignatureVerificationException("Webhook body is not a valid event: " + e.getClass().getSimpleName());
        }
    }

    /** The lowercase hex signature for a body and timestamp, for tests. */
    public static String sign(byte[] payload, String secret, long timestamp) {
        return hex(hmac(secret, timestamp, payload));
    }

    private static byte[] hmac(String secret, long t, byte[] payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            mac.update((t + ".").getBytes(StandardCharsets.US_ASCII));
            return mac.doFinal(payload);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 is not available", e);
        }
    }

    private static String hex(byte[] bytes) {
        char[] digits = "0123456789abcdef".toCharArray();
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            out[i * 2] = digits[(bytes[i] >> 4) & 0xf];
            out[i * 2 + 1] = digits[bytes[i] & 0xf];
        }
        return new String(out);
    }
}
