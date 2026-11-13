package com.ayinbox;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

final class Errors {
    private static final Pattern CODE = Pattern.compile("^[a-z0-9_]+$");

    private Errors() {}

    static ApiException fromResponse(int status, byte[] bytes, Map<String, List<String>> headers) {
        String text = bytes == null ? "" : new String(bytes, StandardCharsets.UTF_8);
        JsonNode body = null;
        try {
            JsonNode parsed = Json.MAPPER.readTree(text);
            if (parsed != null && parsed.isObject()) body = parsed;
        } catch (Exception ignored) {
            // not JSON: keep the raw text
        }
        String code = str(body, "code");
        if (code == null) {
            String err = str(body, "error");
            code = err != null && CODE.matcher(err).matches() ? err : defaultCode(status);
        }
        String message = str(body, "message");
        if (message == null) message = body == null && !text.isBlank() ? text : "";
        String reason = str(body, "reason");
        String requestId = str(body, "request_id");
        if (requestId == null) requestId = header(headers, "x-request-id");
        Long retryAfter = retryAfter(headers, body);

        if (status == 400) return new BadRequestException(status, code, message, reason, requestId, text, headers, retryAfter);
        if (status == 401) return new AuthenticationException(status, code, message, reason, requestId, text, headers, retryAfter);
        if (status == 402) return new PaymentRequiredException(status, code, message, reason, requestId, text, headers, retryAfter);
        if (status == 403) return new PermissionDeniedException(status, code, message, reason, requestId, text, headers, retryAfter);
        if (status == 404) return new NotFoundException(status, code, message, reason, requestId, text, headers, retryAfter);
        if (status == 409) return new ConflictException(status, code, message, reason, requestId, text, headers, retryAfter);
        if (status == 422) return new UnprocessableException(status, code, message, reason, requestId, text, headers, retryAfter);
        if (status == 429) return new RateLimitException(status, code, message, reason, requestId, text, headers, retryAfter);
        if (status == 503) return new ServiceUnavailableException(status, code, message, reason, requestId, text, headers, retryAfter);
        if (status >= 500) return new InternalServerException(status, code, message, reason, requestId, text, headers, retryAfter);
        return new ApiException(status, code, message, reason, requestId, text, headers, retryAfter);
    }

    static String defaultCode(int status) {
        switch (status) {
            case 400: return "bad_request";
            case 401: return "unauthorized";
            case 402: return "insufficient_credits";
            case 403: return "forbidden";
            case 404: return "not_found";
            case 409: return "conflict";
            case 422: return "unprocessable";
            case 429: return "rate_limit_exceeded";
            case 500: return "internal_error";
            case 501: return "not_implemented";
            case 503: return "service_unavailable";
            default: return status >= 500 ? "internal_error" : "http_" + status;
        }
    }

    static Long retryAfter(Map<String, List<String>> headers, JsonNode body) {
        String h = header(headers, "retry-after");
        if (h != null) {
            h = h.trim();
            try {
                return Math.max(0L, Long.parseLong(h));
            } catch (NumberFormatException e) {
                try {
                    ZonedDateTime at = ZonedDateTime.parse(h, DateTimeFormatter.RFC_1123_DATE_TIME);
                    return Math.max(0L, at.toEpochSecond() - System.currentTimeMillis() / 1000L);
                } catch (Exception ignored) {
                    // unparseable header: fall through to the body
                }
            }
        }
        if (body != null) {
            JsonNode ra = body.get("retry_after");
            if (ra != null && ra.isNumber()) return Math.max(0L, (long) Math.ceil(ra.asDouble()));
            JsonNode ms = body.get("retry_after_ms");
            if (ms != null && ms.isNumber()) return Math.max(0L, (long) Math.ceil(ms.asDouble() / 1000.0));
        }
        return null;
    }

    static String header(Map<String, List<String>> headers, String name) {
        if (headers == null) return null;
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(name) && !e.getValue().isEmpty()) return e.getValue().get(0);
        }
        return null;
    }

    private static String str(JsonNode body, String field) {
        if (body == null) return null;
        JsonNode n = body.get(field);
        return n != null && n.isTextual() && !n.asText().isEmpty() ? n.asText() : null;
    }
}
