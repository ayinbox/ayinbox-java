package com.ayinbox;

import java.util.List;
import java.util.Map;

/** Body of {@code POST /campaigns}. Only {@code name} is required; any other field goes through {@link #set(String, Object)}. */
public final class CampaignCreateParams extends Params<CampaignCreateParams> {
    public CampaignCreateParams(String name) {
        set("name", name);
    }

    public CampaignCreateParams description(String v) { return set("description", v); }

    /** Per mailbox per day, 3 to 5000, default 50. */
    public CampaignCreateParams dailyLimit(int v) { return set("daily_limit", v); }

    public CampaignCreateParams stopOnReply(boolean v) { return set("stop_on_reply", v); }

    public CampaignCreateParams openTracking(boolean v) { return set("open_tracking", v); }

    public CampaignCreateParams linkTracking(boolean v) { return set("link_tracking", v); }

    public CampaignCreateParams textOnly(boolean v) { return set("text_only", v); }

    /** IANA zone; empty follows the workspace. */
    public CampaignCreateParams timezone(String v) { return set("timezone", v); }

    /** {@code tags} or {@code explicit}. */
    public CampaignCreateParams senderStrategy(String v) { return set("sender_strategy", v); }

    /** Each {@code {email_account_id, weight?, enabled?}}. */
    public CampaignCreateParams senders(List<Map<String, Object>> v) { return set("senders", v); }

    public CampaignCreateParams emailTagIds(List<String> v) { return set("email_tag_ids", v); }

    public CampaignCreateParams folderIds(List<String> v) { return set("folder_ids", v); }

    /** Steps created and connected in order; each {@code {name, subject, body_plain, body_html, wait_after, thread_reply}}. */
    public CampaignCreateParams steps(List<Map<String, Object>> v) { return set("steps", v); }

    public CampaignCreateParams reviewFirstEmails(boolean v) { return set("review_first_emails", v); }
}
