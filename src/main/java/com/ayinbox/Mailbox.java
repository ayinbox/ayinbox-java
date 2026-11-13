package com.ayinbox;

import java.util.List;

/** A connected mailbox ({@code /emails}). */
public final class Mailbox extends Model {
    private String id;
    private String userId;
    private String organizationId;
    private String workerId;
    private String email;
    private String name;
    private String provider;
    private String status;
    private String mailHost;
    private Integer campaignLimit;
    private Integer minWaitTime;
    private String warmup;
    private String warmupPausedAt;
    private String timezone;
    private List<String> tags;
    private String lastSyncedAt;
    private String createdAt;
    private String updatedAt;

    public String getId() { return id; }

    public String getUserId() { return userId; }

    public String getOrganizationId() { return organizationId; }

    public String getWorkerId() { return workerId; }

    public String getEmail() { return email; }

    public String getName() { return name; }

    public String getProvider() { return provider; }

    public String getStatus() { return status; }

    public String getMailHost() { return mailHost; }

    public Integer getCampaignLimit() { return campaignLimit; }

    public Integer getMinWaitTime() { return minWaitTime; }

    public String getWarmup() { return warmup; }

    public String getWarmupPausedAt() { return warmupPausedAt; }

    public String getTimezone() { return timezone; }

    public List<String> getTags() { return tags; }

    public String getLastSyncedAt() { return lastSyncedAt; }

    public String getCreatedAt() { return createdAt; }

    public String getUpdatedAt() { return updatedAt; }
}
