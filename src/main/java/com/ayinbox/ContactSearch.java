package com.ayinbox;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code POST /contacts/search}: body filters via the setters, plus {@code cursor}, {@code limit} and
 * {@code category} sent in the query. An empty search matches every contact.
 */
public final class ContactSearch extends Params<ContactSearch> {
    final Map<String, Object> query = new LinkedHashMap<>();

    public ContactSearch query(String v) { return set("query", v); }

    /** Contacts in ALL of these campaigns. With exactly one, rows carry {@code campaign_lead}. */
    public ContactSearch campaignIds(List<String> v) { return set("campaign_ids", v); }

    public ContactSearch categoryIds(List<String> v) { return set("category_ids", v); }

    public ContactSearch segmentIds(List<String> v) { return set("segment_ids", v); }

    public ContactSearch subscribed(boolean v) { return set("subscribed", v); }

    public ContactSearch verificationStatus(String v) { return set("verification_status", v); }

    public ContactSearch leadStatus(String v) { return set("lead_status", v); }

    public ContactSearch sortBy(String v) { return set("sort_by", v); }

    public ContactSearch reverse(boolean v) { return set("reverse", v); }

    /** Query parameter. */
    public ContactSearch cursor(String v) { query.put("cursor", v); return this; }

    /** Query parameter; 1 to 100 is recommended. */
    public ContactSearch limit(int v) { query.put("limit", v); return this; }

    /** Query parameter: one label id. */
    public ContactSearch category(String v) { query.put("category", v); return this; }
}
