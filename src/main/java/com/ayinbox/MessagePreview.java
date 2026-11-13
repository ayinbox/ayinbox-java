package com.ayinbox;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/** One unibox row: the newest message of a conversation, or a message in a thread. */
public final class MessagePreview extends Model {
    private String id;
    private String emailId;
    private String threadId;
    private List<String> fromAddr;
    private List<String> toAddr;
    private String subject;
    private String snippet;
    private String internalDate;
    private Boolean seen;
    private Integer messageCount;
    private Boolean hasUnread;
    private JsonNode labels;
    private String answersMailboxId;
    private String campaignId;

    public String getId() { return id; }

    public String getEmailId() { return emailId; }

    public String getThreadId() { return threadId; }

    public List<String> getFromAddr() { return fromAddr; }

    public List<String> getToAddr() { return toAddr; }

    public String getSubject() { return subject; }

    public String getSnippet() { return snippet; }

    public String getInternalDate() { return internalDate; }

    public Boolean getSeen() { return seen; }

    public Integer getMessageCount() { return messageCount; }

    public Boolean getHasUnread() { return hasUnread; }

    public JsonNode getLabels() { return labels; }

    public String getAnswersMailboxId() { return answersMailboxId; }

    public String getCampaignId() { return campaignId; }
}
