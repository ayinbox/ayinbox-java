package com.ayinbox;

import java.util.List;
import java.util.Map;

/** A 400 or 422 validation failure. */
public class ValidationException extends ApiException {
    private static final long serialVersionUID = 1L;

    public ValidationException(int status, String code, String apiMessage, String reason, String requestId,
            String body, Map<String, List<String>> headers, Long retryAfterSeconds) {
        super(status, code, apiMessage, reason, requestId, body, headers, retryAfterSeconds);
    }
}
