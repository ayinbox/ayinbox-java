package com.ayinbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** A successful response: status, headers and raw body. */
public final class RawResponse {
    private final int status;
    private final Map<String, List<String>> headers;
    private final byte[] body;

    RawResponse(int status, Map<String, List<String>> headers, byte[] body) {
        this.status = status;
        this.headers = headers;
        this.body = body == null ? new byte[0] : body;
    }

    public int getStatus() { return status; }

    public Map<String, List<String>> getHeaders() { return headers; }

    /** First value of a header, case-insensitive, or null. */
    public String header(String name) { return Errors.header(headers, name); }

    public byte[] getBody() { return body.clone(); }

    public String text() { return new String(body, StandardCharsets.UTF_8); }

    /** The body as JSON: {@code NullNode} when empty, a {@code TextNode} when it is not JSON. */
    public JsonNode json() {
        String t = text();
        if (t.isBlank()) return NullNode.getInstance();
        try {
            return Json.MAPPER.readTree(t);
        } catch (Exception e) {
            return TextNode.valueOf(t);
        }
    }
}
