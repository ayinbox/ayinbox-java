package com.ayinbox;

import java.util.List;
import java.util.Map;

/** 402: the workspace is out of credits or not entitled. */
public class PaymentRequiredException extends ApiException {
    private static final long serialVersionUID = 1L;

    public PaymentRequiredException(int status, String code, String apiMessage, String reason, String requestId,
            String body, Map<String, List<String>> headers, Long retryAfterSeconds) {
        super(status, code, apiMessage, reason, requestId, body, headers, retryAfterSeconds);
    }
}
