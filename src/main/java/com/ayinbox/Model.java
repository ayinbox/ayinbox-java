package com.ayinbox;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Base of response models. Fields without a typed getter stay readable through {@link #getAdditional()}. */
public abstract class Model {
    @JsonIgnore
    private final Map<String, JsonNode> additional = new LinkedHashMap<>();

    @JsonAnySetter
    void putAdditional(String key, JsonNode value) {
        additional.put(key, value);
    }

    /** Response fields this class does not type, by JSON key. */
    public Map<String, JsonNode> getAdditional() {
        return Collections.unmodifiableMap(additional);
    }

    /** Any field without a typed getter, or null. */
    public JsonNode get(String key) {
        return additional.get(key);
    }
}
