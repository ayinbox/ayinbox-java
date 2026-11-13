package com.ayinbox;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** One page of a list: the rows plus the cursor for the next page. */
public final class Page<T> {
    private final List<T> data;
    private final String nextCursor;
    private final boolean hasMore;
    private final Long total;
    private final JsonNode raw;

    Page(List<T> data, String nextCursor, boolean hasMore, Long total, JsonNode raw) {
        this.data = Collections.unmodifiableList(data);
        this.nextCursor = nextCursor;
        this.hasMore = hasMore;
        this.total = total;
        this.raw = raw;
    }

    static <T> Page<T> parse(JsonNode root, Class<T> type) {
        List<T> rows = new ArrayList<>();
        JsonNode data = root.get("data");
        if (data != null && data.isArray()) {
            for (JsonNode row : data) rows.add(Json.MAPPER.convertValue(row, type));
        }
        JsonNode p = root.get("pagination");
        String next = null;
        boolean more = false;
        Long total = null;
        if (p != null && p.isObject()) {
            JsonNode n = p.get("next_cursor");
            if (n != null && n.isTextual() && !n.asText().isEmpty()) next = n.asText();
            JsonNode m = p.get("has_more");
            more = m != null && m.asBoolean(false);
            JsonNode t = p.get("total");
            if (t != null && t.isNumber()) total = t.asLong();
        }
        return new Page<>(rows, next, more, total, root);
    }

    public List<T> getData() { return data; }

    /** Opaque cursor for the next page, or null. */
    public String getNextCursor() { return nextCursor; }

    public boolean hasMore() { return hasMore; }

    /** Total count when the list reports one, else null. */
    public Long getTotal() { return total; }

    /** The whole response, including extras such as {@code counts} on contact search. */
    public JsonNode getRaw() { return raw; }
}
