package com.ayinbox;

import java.util.List;
import java.util.Map;

/** One contact for {@code POST /contacts}. An existing address is updated, not duplicated. */
public final class ContactInput extends Params<ContactInput> {
    public ContactInput(String email) {
        set("email", email);
    }

    public ContactInput firstName(String v) { return set("first_name", v); }

    public ContactInput lastName(String v) { return set("last_name", v); }

    public ContactInput company(String v) { return set("company", v); }

    public ContactInput phone(String v) { return set("phone", v); }

    /** Campaign ids. Adding a contact to an active campaign queues mail to it. */
    public ContactInput campaigns(List<String> v) { return set("campaigns", v); }

    /** Label ids. */
    public ContactInput categories(List<String> v) { return set("categories", v); }

    public ContactInput segments(List<String> v) { return set("segments", v); }

    /** Merged key by key into the stored fields. */
    public ContactInput customFields(Map<String, String> v) { return set("custom_fields", v); }

    public ContactInput subscribed(boolean v) { return set("subscribed", v); }
}
