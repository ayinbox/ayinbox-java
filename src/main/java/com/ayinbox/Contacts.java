package com.ayinbox;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Contacts. */
public final class Contacts {
    private final AyInbox client;

    Contacts(AyInbox client) { this.client = client; }

    /** One page of {@code POST /contacts/search}. Needs READ_CONTACTS. A read, retried like a GET. */
    public Page<Contact> list() { return list(new ContactSearch(), RequestOptions.NONE); }

    public Page<Contact> list(ContactSearch search) { return list(search, RequestOptions.NONE); }

    public Page<Contact> list(ContactSearch search, RequestOptions options) {
        return Page.parse(search(search.query, search.values, options), Contact.class);
    }

    /** Every matching contact; the same body is sent for every page, as the cursor requires. */
    public AutoPager<Contact> listAll(ContactSearch search) {
        Map<String, Object> base = new LinkedHashMap<>(search.query);
        base.remove("cursor");
        Map<String, Object> body = new LinkedHashMap<>(search.values);
        return new AutoPager<>(cursor -> {
            Map<String, Object> q = new LinkedHashMap<>(base);
            if (cursor != null) q.put("cursor", cursor);
            return Page.parse(search(q, body, RequestOptions.NONE), Contact.class);
        });
    }

    public AutoPager<Contact> listAll() { return listAll(new ContactSearch()); }

    private JsonNode search(Map<String, Object> query, Map<String, Object> body, RequestOptions options) {
        return client.execute("POST", "/contacts/search", query, body, options, true).json();
    }

    /** {@code GET /contacts/{id}}. Needs READ_CONTACTS. */
    public ContactDetail get(String id) { return get(id, RequestOptions.NONE); }

    public ContactDetail get(String id, RequestOptions options) {
        return client.get("/contacts/" + AyInbox.seg(id), null, options, ContactDetail.class);
    }

    /**
     * {@code POST /contacts}: creates or updates (by address) one contact. Needs WRITE_CONTACTS.
     * Adding a contact to an active campaign can send mail.
     */
    public List<Contact> create(ContactInput contact) { return create(contact, RequestOptions.NONE); }

    public List<Contact> create(ContactInput contact, RequestOptions options) {
        return rows(client.execute("POST", "/contacts", null, contact, options, false).json());
    }

    /** Creates or updates up to 10,240 contacts in one call. */
    public List<Contact> create(List<ContactInput> contacts, RequestOptions options) {
        return rows(client.execute("POST", "/contacts", null, contacts, options, false).json());
    }

    private static List<Contact> rows(JsonNode root) {
        if (root == null || !root.isArray()) return Collections.emptyList();
        List<Contact> out = new ArrayList<>();
        for (JsonNode n : root) out.add(AyInbox.convert(n, Contact.class));
        return out;
    }

    /** {@code PATCH /contacts/{id}}. Needs WRITE_CONTACTS. */
    public Contact update(String id, ContactUpdate update) { return update(id, update, RequestOptions.NONE); }

    public Contact update(String id, ContactUpdate update, RequestOptions options) {
        return AyInbox.convert(
                client.execute("PATCH", "/contacts/" + AyInbox.seg(id), null, update, options, false).json(), Contact.class);
    }

    /** {@code DELETE /contacts/{id}}. Needs WRITE_CONTACTS. */
    public void delete(String id) { delete(id, RequestOptions.NONE); }

    public void delete(String id, RequestOptions options) {
        client.execute("DELETE", "/contacts/" + AyInbox.seg(id), null, null, options, false);
    }
}
