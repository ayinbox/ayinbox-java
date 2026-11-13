package com.ayinbox;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/** The API answered with a non-2xx status. Branch on {@link #getStatus()} and {@link #getCode()}, never on the message. */
public class ApiException extends AyInboxException {
    private static final long serialVersionUID = 1L;

    private final int status;
    private final String code;
    private final String apiMessage;
    private final String reason;
    private final String requestId;
    private final String body;
    private final transient Map<String, List<String>> headers;
    private final Long retryAfterSeconds;

    public ApiException(int status, String code, String apiMessage, String reason, String requestId,
                        String body, Map<String, List<String>> headers, Long retryAfterSeconds) {
        super(describe(status, code, apiMessage, requestId));
        this.status = status;
        this.code = code;
        this.apiMessage = apiMessage;
        this.reason = reason;
        this.requestId = requestId;
        this.body = body;
        this.headers = headers == null ? Collections.emptyMap() : headers;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    private static String describe(int status, String code, String message, String requestId) {
        StringBuilder sb = new StringBuilder().append(status).append(' ').append(code);
        if (message != null && !message.isEmpty()) sb.append(": ").append(message);
        if (requestId != null) sb.append(" (request ").append(requestId).append(')');
        return sb.toString();
    }

    /** HTTP status. */
    public int getStatus() { return status; }

    /** Stable machine-readable code, for example {@code not_found}. */
    public String getCode() { return code; }

    /** The API's human-readable message. */
    public String getApiMessage() { return apiMessage; }

    /** Finer key on a few codes, or null. */
    public String getReason() { return reason; }

    /** Request id from the body or the {@code X-Request-Id} header, or null. */
    public String getRequestId() { return requestId; }

    /** The raw response body as text. */
    public String getBody() { return body; }

    /** Response headers, keys lowercased. */
    public Map<String, List<String>> getHeaders() { return headers; }

    /** Seconds the API asked to wait, from {@code Retry-After}, {@code retry_after} or {@code retry_after_ms}, or null. */
    public Long getRetryAfterSeconds() { return retryAfterSeconds; }
}
