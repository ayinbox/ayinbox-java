package com.ayinbox;

/** Mailboxes ({@code /emails}). */
public final class Mailboxes {
    private final AyInbox client;

    Mailboxes(AyInbox client) { this.client = client; }

    /** One page of {@code GET /emails}. Needs READ_EMAILS. */
    public Page<Mailbox> list() { return list(new MailboxListParams(), RequestOptions.NONE); }

    public Page<Mailbox> list(MailboxListParams params) { return list(params, RequestOptions.NONE); }

    public Page<Mailbox> list(MailboxListParams params, RequestOptions options) {
        return Page.parse(client.execute("GET", "/emails", params.values, null, options, false).json(), Mailbox.class);
    }

    public AutoPager<Mailbox> listAll() { return listAll(new MailboxListParams()); }

    public AutoPager<Mailbox> listAll(MailboxListParams params) {
        return client.pager("/emails", params.values, Mailbox.class);
    }
}
