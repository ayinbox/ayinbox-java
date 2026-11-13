package com.ayinbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The AY Inbox API client. Thread-safe; create one and share it.
 *
 * <pre>{@code
 * AyInbox client = AyInbox.builder().build(); // reads AYINBOX_API_KEY
 * Identity me = client.me().get();
 * }</pre>
 */
public final class AyInbox {
    /** Library version, sent in the User-Agent. */
    public static final String VERSION = "0.1.0";
    /** Production base URL. */
    public static final String DEFAULT_BASE_URL = "https://api.ayinbox.com/v1";

    private static final long MAX_RETRY_AFTER_SECONDS = 120;
    private static final double BACKOFF_BASE_SECONDS = 0.5;
    private static final double BACKOFF_CAP_SECONDS = 8;

    private final String apiKey;
    private final String baseUrl;
    private final Duration timeout;
    private final int maxRetries;
    private final HttpClient http;

    private final Me me;
    private final Campaigns campaigns;
    private final Contacts contacts;
    private final Mailboxes mailboxes;
    private final Unibox unibox;
    private final Webhooks webhooks;

    private AyInbox(Builder b) {
        String key = b.apiKey != null ? b.apiKey : System.getenv("AYINBOX_API_KEY");
        if (key == null || key.isBlank()) {
            throw new AyInboxException("No API key: pass apiKey(...) or set AYINBOX_API_KEY");
        }
        String url = b.baseUrl != null ? b.baseUrl : System.getenv("AYINBOX_BASE_URL");
        if (url == null || url.isBlank()) url = DEFAULT_BASE_URL;
        while (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        if (b.maxRetries < 0) throw new IllegalArgumentException("maxRetries must be 0 or more");
        this.apiKey = key.trim();
        this.baseUrl = url;
        this.timeout = b.timeout;
        this.maxRetries = b.maxRetries;
        this.http = b.httpClient != null ? b.httpClient
                : HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.me = new Me(this);
        this.campaigns = new Campaigns(this);
        this.contacts = new Contacts(this);
        this.mailboxes = new Mailboxes(this);
        this.unibox = new Unibox(this);
        this.webhooks = new Webhooks();
    }

    public static Builder builder() { return new Builder(); }

    public Me me() { return me; }

    public Campaigns campaigns() { return campaigns; }

    public Contacts contacts() { return contacts; }

    public Mailboxes mailboxes() { return mailboxes; }

    public Unibox unibox() { return unibox; }

    public Webhooks webhooks() { return webhooks; }

    public String getBaseUrl() { return baseUrl; }

    @Override
    public String toString() {
        return "AyInbox{baseUrl=" + baseUrl + ", apiKey=[redacted]}";
    }

    /** Calls any route and returns its JSON ({@code NullNode} for an empty body). */
    public JsonNode request(String method, String path) {
        return request(method, path, null, null, RequestOptions.NONE);
    }

    /**
     * Calls any route with the same auth, retries and error mapping as the typed methods.
     *
     * @param path  relative to the base URL, for example {@code /unibox/count}
     * @param query query parameters; null values are skipped
     * @param body  any value Jackson can serialize, or null
     */
    public JsonNode request(String method, String path, Map<String, ?> query, Object body, RequestOptions options) {
        return requestRaw(method, path, query, body, options).json();
    }

    /** Like {@link #request} but returns status, headers and raw bytes. */
    public RawResponse requestRaw(String method, String path, Map<String, ?> query, Object body, RequestOptions options) {
        return execute(method, path, query, body, options, false);
    }

    /** Walks every page of any paginated route, yielding each row as JSON. */
    public AutoPager<JsonNode> paginate(String path, Map<String, ?> query) {
        return pager(path, query, JsonNode.class);
    }

    <T> AutoPager<T> pager(String path, Map<String, ?> query, Class<T> type) {
        Map<String, Object> base = query == null ? new LinkedHashMap<>() : new LinkedHashMap<>(query);
        base.remove("cursor");
        return new AutoPager<>(cursor -> {
            Map<String, Object> q = new LinkedHashMap<>(base);
            if (cursor != null) q.put("cursor", cursor);
            return Page.parse(execute("GET", path, q, null, RequestOptions.NONE, false).json(), type);
        });
    }

    <T> T get(String path, Map<String, ?> query, RequestOptions options, Class<T> type) {
        return convert(execute("GET", path, query, null, options, false).json(), type);
    }

    static <T> T convert(JsonNode node, Class<T> type) {
        return Json.MAPPER.convertValue(node, type);
    }

    static String seg(String id) {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("id is required");
        return URLEncoder.encode(id, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /** @param readOnly a POST that only reads, safe to retry without an idempotency key */
    RawResponse execute(String method, String path, Map<String, ?> query, Object body, RequestOptions options, boolean readOnly) {
        RequestOptions opts = options == null ? RequestOptions.NONE : options;
        String m = method.toUpperCase(Locale.ROOT);
        URI uri = URI.create(baseUrl + (path.startsWith("/") ? path : "/" + path) + queryString(query));
        byte[] payload = null;
        if (body != null) {
            try {
                payload = body instanceof byte[] ? (byte[]) body : Json.MAPPER.writeValueAsBytes(unwrap(body));
            } catch (JsonProcessingException e) {
                throw new AyInboxException("Could not encode the request body", e);
            }
        }
        String idem = opts.getIdempotencyKey();
        boolean unsafe = ("POST".equals(m) || "PATCH".equals(m)) && !readOnly && (idem == null || idem.isEmpty());
        Duration callTimeout = opts.getTimeout() != null ? opts.getTimeout() : timeout;

        for (int attempt = 0; ; attempt++) {
            HttpRequest.Builder rb = HttpRequest.newBuilder(uri)
                    .timeout(callTimeout)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("User-Agent", "ayinbox-java/" + VERSION)
                    .header("Accept", "application/json");
            if (payload != null) {
                rb.header("Content-Type", "application/json");
                rb.method(m, HttpRequest.BodyPublishers.ofByteArray(payload));
            } else {
                rb.method(m, HttpRequest.BodyPublishers.noBody());
            }
            if (idem != null && !idem.isEmpty()) rb.header("Idempotency-Key", idem);
            for (Map.Entry<String, String> h : opts.getHeaders().entrySet()) rb.setHeader(h.getKey(), h.getValue());

            AyInboxException failure;
            boolean retryable;
            Long retryAfter = null;
            try {
                HttpResponse<byte[]> resp = http.send(rb.build(), HttpResponse.BodyHandlers.ofByteArray());
                int status = resp.statusCode();
                if (status >= 200 && status < 300) {
                    return new RawResponse(status, resp.headers().map(), resp.body());
                }
                ApiException err = Errors.fromResponse(status, resp.body(), resp.headers().map());
                failure = err;
                retryAfter = err.getRetryAfterSeconds();
                retryable = status == 429
                        || (!unsafe && (status == 502 || status == 503 || status == 504)
                            && !"idempotency_outcome_unknown".equals(err.getCode()));
            } catch (HttpConnectTimeoutException e) {
                failure = new ApiConnectionException("Connection to the AY Inbox API timed out", e);
                retryable = true;
            } catch (HttpTimeoutException e) {
                failure = new ApiTimeoutException("The AY Inbox API call timed out after " + callTimeout, e);
                retryable = !unsafe;
            } catch (ConnectException e) {
                failure = new ApiConnectionException("Could not connect to the AY Inbox API", e);
                retryable = true;
            } catch (IOException e) {
                failure = new ApiConnectionException("Network error calling the AY Inbox API: " + e.getClass().getSimpleName(), e);
                retryable = !unsafe;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ApiConnectionException("Interrupted while calling the AY Inbox API", e);
            }
            if (!retryable || attempt >= maxRetries) throw failure;
            sleep(delayMillis(attempt, retryAfter));
        }
    }

    private static Object unwrap(Object body) {
        if (body instanceof Params) return ((Params<?>) body).values;
        if (body instanceof Iterable) {
            java.util.List<Object> out = new java.util.ArrayList<>();
            for (Object o : (Iterable<?>) body) out.add(unwrap(o));
            return out;
        }
        return body;
    }

    static long delayMillis(int attempt, Long retryAfterSeconds) {
        if (retryAfterSeconds != null) return Math.min(retryAfterSeconds, MAX_RETRY_AFTER_SECONDS) * 1000L;
        double ceiling = Math.min(BACKOFF_CAP_SECONDS, BACKOFF_BASE_SECONDS * Math.pow(2, attempt));
        return (long) (ThreadLocalRandom.current().nextDouble() * ceiling * 1000);
    }

    private static void sleep(long millis) {
        if (millis <= 0) return;
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiConnectionException("Interrupted while waiting to retry", e);
        }
    }

    private static String queryString(Map<String, ?> query) {
        if (query == null || query.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, ?> e : query.entrySet()) {
            if (e.getValue() == null) continue;
            sb.append(sb.length() == 0 ? '?' : '&')
                    .append(enc(e.getKey())).append('=').append(enc(String.valueOf(e.getValue())));
        }
        return sb.toString();
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /** Builder for {@link AyInbox}. */
    public static final class Builder {
        private String apiKey;
        private String baseUrl;
        private Duration timeout = Duration.ofSeconds(30);
        private int maxRetries = 2;
        private HttpClient httpClient;

        private Builder() {}

        /** Defaults to {@code AYINBOX_API_KEY}. */
        public Builder apiKey(String apiKey) { this.apiKey = apiKey; return this; }

        /** Defaults to {@code AYINBOX_BASE_URL}, then {@code https://api.ayinbox.com/v1}. */
        public Builder baseUrl(String baseUrl) { this.baseUrl = baseUrl; return this; }

        /** Per attempt, default 30 seconds. */
        public Builder timeout(Duration timeout) { this.timeout = timeout; return this; }

        /** Default 2; 0 disables retries. */
        public Builder maxRetries(int maxRetries) { this.maxRetries = maxRetries; return this; }

        /** A shared or customised client (proxy, executor, TLS). */
        public Builder httpClient(HttpClient httpClient) { this.httpClient = httpClient; return this; }

        public AyInbox build() { return new AyInbox(this); }
    }
}
