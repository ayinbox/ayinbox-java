package com.ayinbox;

/** Query for {@code GET /unibox}. Other filters go through {@link #set(String, Object)}. */
public final class ThreadListParams extends Params<ThreadListParams> {
    /** {@code inbox}, {@code sent}, {@code drafts}, {@code archive}, {@code spam} or {@code trash}. */
    public ThreadListParams folder(String folder) { return set("folder", folder); }

    public ThreadListParams unseen(boolean unseen) { return set("unseen", unseen); }

    public ThreadListParams campaignId(String id) { return set("campaign_id", id); }

    public ThreadListParams contactId(String id) { return set("contact_id", id); }

    public ThreadListParams emailId(String id) { return set("email_id", id); }

    public ThreadListParams cursor(String cursor) { return set("cursor", cursor); }

    /** 1 to 100, default 50. */
    public ThreadListParams limit(int limit) { return set("limit", limit); }
}
