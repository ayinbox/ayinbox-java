package com.ayinbox;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Options for one call: idempotency key, timeout override and extra headers. */
public final class RequestOptions {
    /** No options. */
    public static final RequestOptions NONE = builder().build();

    private final String idempotencyKey;
    private final Duration timeout;
    private final Map<String, String> headers;

    private RequestOptions(Builder b) {
        this.idempotencyKey = b.idempotencyKey;
        this.timeout = b.timeout;
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(b.headers));
    }

    public static Builder builder() { return new Builder(); }

    /** Shortcut for options carrying only an idempotency key. */
    public static RequestOptions idempotencyKey(String key) { return builder().idempotencyKey(key).build(); }

    public String getIdempotencyKey() { return idempotencyKey; }

    public Duration getTimeout() { return timeout; }

    public Map<String, String> getHeaders() { return headers; }

    /** Builder for {@link RequestOptions}. */
    public static final class Builder {
        private String idempotencyKey;
        private Duration timeout;
        private final Map<String, String> headers = new LinkedHashMap<>();

        private Builder() {}

        /** Sent as {@code Idempotency-Key}; reused on every retry of the call. */
        public Builder idempotencyKey(String key) { this.idempotencyKey = key; return this; }

        public Builder timeout(Duration timeout) { this.timeout = timeout; return this; }

        public Builder header(String name, String value) { headers.put(name, value); return this; }

        public RequestOptions build() { return new RequestOptions(this); }
    }
}
