package com.ayinbox;

/** Query for {@code GET /unibox/thread}. */
public final class ThreadParams extends Params<ThreadParams> {
    public ThreadParams(String threadId) {
        set("thread_id", threadId);
    }

    /** The mailbox; omit to read across every mailbox. */
    public ThreadParams emailId(String id) { return set("email_id", id); }

    public ThreadParams cursor(String cursor) { return set("cursor", cursor); }

    /** 1 to 500, default 500. */
    public ThreadParams limit(int limit) { return set("limit", limit); }
}
