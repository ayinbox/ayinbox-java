package com.ayinbox;

import java.util.List;
import java.util.Map;

/** 401: the credential is missing, wrong or no longer valid. */
public class AuthenticationException extends ApiException {
    private static final long serialVersionUID = 1L;

    public AuthenticationException(int status, String code, String apiMessage, String reason, String requestId,
            String body, Map<String, List<String>> headers, Long retryAfterSeconds) {
        super(status, code, apiMessage, reason, requestId, body, headers, retryAfterSeconds);
    }
}
