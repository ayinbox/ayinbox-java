package com.ayinbox;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base of every parameter object: an ordered map of JSON keys to values.
 * {@link #set(String, Object)} reaches any field the typed setters do not cover; a null value is sent as JSON null.
 */
public abstract class Params<S extends Params<S>> {
    final Map<String, Object> values = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    private S self() { return (S) this; }

    /** Sets any field by its JSON key. */
    public S set(String key, Object value) {
        values.put(key, value);
        return self();
    }

    /** Removes a field so it is not sent. */
    public S unset(String key) {
        values.remove(key);
        return self();
    }

    /** The fields as they will be sent. */
    public Map<String, Object> toMap() {
        return Collections.unmodifiableMap(values);
    }
}
