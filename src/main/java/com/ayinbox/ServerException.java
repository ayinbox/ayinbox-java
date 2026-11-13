package com.ayinbox;

import java.util.List;
import java.util.Map;

/** A 5xx answer. */
public class ServerException extends ApiException {
    private static final long serialVersionUID = 1L;

    public ServerException(int status, String code, String apiMessage, String reason, String requestId,
            String body, Map<String, List<String>> headers, Long retryAfterSeconds) {
        super(status, code, apiMessage, reason, requestId, body, headers, retryAfterSeconds);
    }
}
