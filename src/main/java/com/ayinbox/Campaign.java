package com.ayinbox;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/** A campaign. Timestamps are RFC 3339 strings; untyped fields are in {@link #getAdditional()}. */
public final class Campaign extends Model {
    private String id;
    private String userId;
    private String organizationId;
    private String name;
    private String description;
    private String status;
    private Boolean stopOnReply;
    private Boolean stopOnMeeting;
    private Boolean openTracking;
    private Boolean linkTracking;
    private Boolean textOnly;
    private Boolean unsubscribeHeader;
    private Boolean riskyEmails;
    private Integer dailyLimit;
    private String unsubscribeMode;
    private List<String> cc;
    private List<String> bcc;
    private String startDate;
    private String endDate;
    private String timezone;
    private String effectiveTimezone;
    private JsonNode scheduleWindows;
    private List<String> emailTags;
    private List<String> folders;
    private String senderStrategy;
    private String rotationMode;
    private JsonNode senders;
    private Integer maxNewLeadsPerDay;
    private Boolean continuous;
    private Boolean reviewFirstEmails;
    private String idleSince;
    private String lastStatusChangeAt;
    private String createdAt;
    private String updatedAt;

    public String getId() { return id; }

    public String getUserId() { return userId; }

    public String getOrganizationId() { return organizationId; }

    public String getName() { return name; }

    public String getDescription() { return description; }

    public String getStatus() { return status; }

    public Boolean getStopOnReply() { return stopOnReply; }

    public Boolean getStopOnMeeting() { return stopOnMeeting; }

    public Boolean getOpenTracking() { return openTracking; }

    public Boolean getLinkTracking() { return linkTracking; }

    public Boolean getTextOnly() { return textOnly; }

    public Boolean getUnsubscribeHeader() { return unsubscribeHeader; }

    public Boolean getRiskyEmails() { return riskyEmails; }

    public Integer getDailyLimit() { return dailyLimit; }

    public String getUnsubscribeMode() { return unsubscribeMode; }

    public List<String> getCc() { return cc; }

    public List<String> getBcc() { return bcc; }

    public String getStartDate() { return startDate; }

    public String getEndDate() { return endDate; }

    public String getTimezone() { return timezone; }

    public String getEffectiveTimezone() { return effectiveTimezone; }

    public JsonNode getScheduleWindows() { return scheduleWindows; }

    public List<String> getEmailTags() { return emailTags; }

    public List<String> getFolders() { return folders; }

    public String getSenderStrategy() { return senderStrategy; }

    public String getRotationMode() { return rotationMode; }

    public JsonNode getSenders() { return senders; }

    public Integer getMaxNewLeadsPerDay() { return maxNewLeadsPerDay; }

    public Boolean getContinuous() { return continuous; }

    public Boolean getReviewFirstEmails() { return reviewFirstEmails; }

    public String getIdleSince() { return idleSince; }

    public String getLastStatusChangeAt() { return lastStatusChangeAt; }

    public String getCreatedAt() { return createdAt; }

    public String getUpdatedAt() { return updatedAt; }
}
