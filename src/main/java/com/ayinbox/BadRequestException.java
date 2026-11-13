package com.ayinbox;

import java.util.List;
import java.util.Map;

/** 400 Bad Request. */
public class BadRequestException extends ValidationException {
    private static final long serialVersionUID = 1L;

    public BadRequestException(int status, String code, String apiMessage, String reason, String requestId,
            String body, Map<String, List<String>> headers, Long retryAfterSeconds) {
        super(status, code, apiMessage, reason, requestId, body, headers, retryAfterSeconds);
    }
}
