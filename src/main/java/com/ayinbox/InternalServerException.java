package com.ayinbox;

import java.util.List;
import java.util.Map;

/** 500, 501, 502, 504 and any other 5xx except 503. */
public class InternalServerException extends ServerException {
    private static final long serialVersionUID = 1L;

    public InternalServerException(int status, String code, String apiMessage, String reason, String requestId,
            String body, Map<String, List<String>> headers, Long retryAfterSeconds) {
        super(status, code, apiMessage, reason, requestId, body, headers, retryAfterSeconds);
    }
}
