package com.ayinbox;

import com.fasterxml.jackson.databind.JsonNode;

/** A verified webhook delivery. {@code id} equals the {@code X-Ayinbox-Event-Id} header. */
public final class WebhookEvent extends Model {
    private String id;
    private String eventType;
    private String organizationId;
    private String createdAt;
    private JsonNode data;

    public String getId() { return id; }

    public String getEventType() { return eventType; }

    public String getOrganizationId() { return organizationId; }

    public String getCreatedAt() { return createdAt; }

    public JsonNode getData() { return data; }
}
