package com.ayinbox;

/** Query for {@code GET /campaigns}. */
public final class CampaignListParams extends Params<CampaignListParams> {
    /** Name search. */
    public CampaignListParams q(String q) { return set("q", q); }

    /** {@code draft}, {@code active}, {@code paused} or {@code completed}. */
    public CampaignListParams status(String status) { return set("status", status); }

    public CampaignListParams folder(String folderId) { return set("folder", folderId); }

    public CampaignListParams cursor(String cursor) { return set("cursor", cursor); }

    /** Page size; 1 to 100 is recommended. */
    public CampaignListParams limit(int limit) { return set("limit", limit); }
}
