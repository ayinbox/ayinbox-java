package com.ayinbox;

/** Query for {@code GET /emails}. */
public final class MailboxListParams extends Params<MailboxListParams> {
    public MailboxListParams q(String q) { return set("q", q); }

    public MailboxListParams tag(String tagId) { return set("tag", tagId); }

    public MailboxListParams cursor(String cursor) { return set("cursor", cursor); }

    /** Page size; 1 to 100 is recommended. */
    public MailboxListParams limit(int limit) { return set("limit", limit); }
}
