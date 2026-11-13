package com.ayinbox;

import com.fasterxml.jackson.databind.JsonNode;

/** Campaigns. Every other campaign route is reachable through {@link AyInbox#request}. */
public final class Campaigns {
    private final AyInbox client;

    Campaigns(AyInbox client) { this.client = client; }

    /** One page of {@code GET /campaigns}. Needs READ_CAMPAIGNS. */
    public Page<Campaign> list() { return list(new CampaignListParams(), RequestOptions.NONE); }

    public Page<Campaign> list(CampaignListParams params) { return list(params, RequestOptions.NONE); }

    public Page<Campaign> list(CampaignListParams params, RequestOptions options) {
        JsonNode root = client.execute("GET", "/campaigns", params.values, null, options, false).json();
        return Page.parse(root, Campaign.class);
    }

    /** Every campaign, page by page. */
    public AutoPager<Campaign> listAll() { return listAll(new CampaignListParams()); }

    public AutoPager<Campaign> listAll(CampaignListParams params) {
        return client.pager("/campaigns", params.values, Campaign.class);
    }

    /** {@code GET /campaigns/{id}}. Needs READ_CAMPAIGNS. */
    public Campaign get(String id) { return get(id, RequestOptions.NONE); }

    public Campaign get(String id, RequestOptions options) {
        return client.get("/campaigns/" + AyInbox.seg(id), null, options, Campaign.class);
    }

    /** {@code POST /campaigns}. Needs WRITE_CAMPAIGNS. Creating never sends mail. */
    public Campaign create(CampaignCreateParams params) { return create(params, RequestOptions.NONE); }

    public Campaign create(CampaignCreateParams params, RequestOptions options) {
        return AyInbox.convert(client.execute("POST", "/campaigns", null, params, options, false).json(), Campaign.class);
    }

    /**
     * {@code POST /campaigns/{id}/stop}: pauses an active campaign. Needs WRITE_CAMPAIGNS, START_CAMPAIGNS or
     * SEND_CAMPAIGNS. Sends nothing. Returns {@code {"status": "stopped"}}.
     */
    public JsonNode pause(String id) { return pause(id, RequestOptions.NONE); }

    public JsonNode pause(String id, RequestOptions options) {
        return client.execute("POST", "/campaigns/" + AyInbox.seg(id) + "/stop", null, null, options, false).json();
    }
}
