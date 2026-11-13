package com.ayinbox;

/** The unified inbox. Needs READ_UNIBOX and an active trial or paid plan. */
public final class Unibox {
    private final AyInbox client;

    Unibox(AyInbox client) { this.client = client; }

    /** One page of {@code GET /unibox}: one row per conversation. */
    public Page<MessagePreview> listThreads() { return listThreads(new ThreadListParams(), RequestOptions.NONE); }

    public Page<MessagePreview> listThreads(ThreadListParams params) { return listThreads(params, RequestOptions.NONE); }

    public Page<MessagePreview> listThreads(ThreadListParams params, RequestOptions options) {
        return Page.parse(client.execute("GET", "/unibox", params.values, null, options, false).json(), MessagePreview.class);
    }

    public AutoPager<MessagePreview> listAllThreads() { return listAllThreads(new ThreadListParams()); }

    public AutoPager<MessagePreview> listAllThreads(ThreadListParams params) {
        return client.pager("/unibox", params.values, MessagePreview.class);
    }

    /** One page of {@code GET /unibox/thread}. Rows are previews; read bodies with {@code GET /unibox/{id}}. */
    public Page<MessagePreview> getThread(String threadId) { return getThread(new ThreadParams(threadId), RequestOptions.NONE); }

    public Page<MessagePreview> getThread(ThreadParams params) { return getThread(params, RequestOptions.NONE); }

    public Page<MessagePreview> getThread(ThreadParams params, RequestOptions options) {
        return Page.parse(client.execute("GET", "/unibox/thread", params.values, null, options, false).json(), MessagePreview.class);
    }

    /** Every message of a thread, page by page. */
    public AutoPager<MessagePreview> getThreadAll(ThreadParams params) {
        return client.pager("/unibox/thread", params.values, MessagePreview.class);
    }
}
