package com.ayinbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class WebhookSignatureTest {
    private static final JsonNode VECTOR;

    static {
        try {
            VECTOR = Json.MAPPER.readTree(Fixtures.read("webhook_signature.json"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static final String SECRET = VECTOR.get("secret").asText();
    private static final long T = VECTOR.get("timestamp").asLong();
    private static final byte[] BODY = VECTOR.get("body").asText().getBytes(StandardCharsets.UTF_8);
    private static final String HEADER = VECTOR.get("header").asText();

    @Test
    void computesTheExpectedSignature() {
        assertEquals(VECTOR.get("expected_v1").asText(), WebhookSignature.sign(BODY, SECRET, T));
    }

    @Test
    void validVectorPasses() {
        assertTrue(WebhookSignature.verify(BODY, HEADER, SECRET, 300, T));
        assertTrue(WebhookSignature.verify(BODY, HEADER, SECRET, 300, T + 300));
    }

    @Test
    void tamperedBodyFails() {
        byte[] tampered = new String(BODY, StandardCharsets.UTF_8).replace("{}", "{\"x\":1}").getBytes(StandardCharsets.UTF_8);
        assertFalse(WebhookSignature.verify(tampered, HEADER, SECRET, 300, T));
    }

    @Test
    void wrongSecretFails() {
        assertFalse(WebhookSignature.verify(BODY, HEADER, "whsec_other", 300, T));
        assertFalse(WebhookSignature.verify(BODY, HEADER, "test_secret", 300, T));
    }

    @Test
    void outsideToleranceFails() {
        assertFalse(WebhookSignature.verify(BODY, HEADER, SECRET, 300, T + 301));
        assertFalse(WebhookSignature.verify(BODY, HEADER, SECRET, 300, T - 301));
    }

    @Test
    void malformedHeadersFail() {
        String v1 = VECTOR.get("expected_v1").asText();
        assertFalse(WebhookSignature.verify(BODY, "v1=" + v1, SECRET, 300, T));
        assertFalse(WebhookSignature.verify(BODY, "t=" + T, SECRET, 300, T));
        assertFalse(WebhookSignature.verify(BODY, "", SECRET, 300, T));
        assertFalse(WebhookSignature.verify(BODY, "t=abc,v1=" + v1, SECRET, 300, T));
    }

    @Test
    void anyMatchingV1PassesAndOtherSchemesAreIgnored() {
        String v1 = VECTOR.get("expected_v1").asText();
        assertTrue(WebhookSignature.verify(BODY, "t=" + T + ",v0=abc,v1=deadbeef,v1=" + v1, SECRET, 300, T));
    }

    @Test
    void constructEventParsesOrThrows() {
        WebhookEvent e = WebhookSignature.constructEvent(BODY, HEADER, SECRET, 300, T);
        assertEquals("campaign.reply_received", e.getEventType());
        assertEquals("00000000-0000-4000-8000-000000000001", e.getId());
        assertTrue(e.getData().isObject());
        assertThrows(SignatureVerificationException.class,
                () -> WebhookSignature.constructEvent(BODY, HEADER, "whsec_wrong", 300, T));
    }

    @Test
    void zeroToleranceIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> WebhookSignature.verify(BODY, HEADER, SECRET, 0, T));
    }
}
