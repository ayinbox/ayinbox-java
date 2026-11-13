package com.ayinbox;

import java.util.List;
import java.util.Map;

/** 429 Too Many Requests. {@link #getRetryAfterSeconds()} says how long to wait. */
public class RateLimitException extends ApiException {
    private static final long serialVersionUID = 1L;

    public RateLimitException(int status, String code, String apiMessage, String reason, String requestId,
            String body, Map<String, List<String>> headers, Long retryAfterSeconds) {
        super(status, code, apiMessage, reason, requestId, body, headers, retryAfterSeconds);
    }
}
