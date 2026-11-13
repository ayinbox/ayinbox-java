package com.ayinbox;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

/** A local HTTP server that answers queued responses and records every request. */
final class MockServer implements AutoCloseable {
    static final class Recorded {
        final String method;
        final String path;
        final String query;
        final Map<String, List<String>> headers;
        final String body;

        Recorded(String method, String path, String query, Map<String, List<String>> headers, String body) {
            this.method = method;
            this.path = path;
            this.query = query;
            this.headers = headers;
            this.body = body;
        }

        String header(String name) {
            for (Map.Entry<String, List<String>> e : headers.entrySet()) {
                if (e.getKey().equalsIgnoreCase(name)) return e.getValue().get(0);
            }
            return null;
        }

        int headerCount(String name) {
            int n = 0;
            for (Map.Entry<String, List<String>> e : headers.entrySet()) {
                if (e.getKey().equalsIgnoreCase(name)) n += e.getValue().size();
            }
            return n;
        }
    }

    private static final class Canned {
        final int status;
        final String body;
        final Map<String, String> headers;
        final long delayMillis;

        Canned(int status, String body, Map<String, String> headers, long delayMillis) {
            this.status = status;
            this.body = body;
            this.headers = headers;
            this.delayMillis = delayMillis;
        }
    }

    private final HttpServer server;
    private final ConcurrentLinkedQueue<Canned> queue = new ConcurrentLinkedQueue<>();
    final List<Recorded> requests = Collections.synchronizedList(new ArrayList<>());

    MockServer() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/", exchange -> {
            byte[] in;
            try (InputStream is = exchange.getRequestBody()) {
                in = is.readAllBytes();
            }
            requests.add(new Recorded(exchange.getRequestMethod(), exchange.getRequestURI().getPath(),
                    exchange.getRequestURI().getRawQuery(), new LinkedHashMap<>(exchange.getRequestHeaders()),
                    new String(in, StandardCharsets.UTF_8)));
            Canned c = queue.poll();
            if (c == null) c = new Canned(500, "{\"error\":\"no canned response\"}", Collections.emptyMap(), 0);
            if (c.delayMillis > 0) {
                try {
                    Thread.sleep(c.delayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.getResponseHeaders().set("API-Version", "v1");
            for (Map.Entry<String, String> h : c.headers.entrySet()) exchange.getResponseHeaders().set(h.getKey(), h.getValue());
            byte[] out = c.body == null ? new byte[0] : c.body.getBytes(StandardCharsets.UTF_8);
            try {
                exchange.sendResponseHeaders(c.status, out.length == 0 ? -1 : out.length);
                if (out.length > 0) {
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(out);
                    }
                }
            } catch (IOException ignored) {
                // the client gave up (timeout test)
            } finally {
                exchange.close();
            }
        });
        server.start();
    }

    String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";
    }

    MockServer enqueue(int status, String body, String... headerPairs) {
        Map<String, String> h = new LinkedHashMap<>();
        for (int i = 0; i + 1 < headerPairs.length; i += 2) h.put(headerPairs[i], headerPairs[i + 1]);
        queue.add(new Canned(status, body, h, 0));
        return this;
    }

    MockServer enqueueDelayed(long delayMillis, int status, String body) {
        queue.add(new Canned(status, body, Collections.emptyMap(), delayMillis));
        return this;
    }

    MockServer fixture(int status, String name, String... headerPairs) {
        return enqueue(status, Fixtures.read(name), headerPairs);
    }

    Recorded last() {
        return requests.get(requests.size() - 1);
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
