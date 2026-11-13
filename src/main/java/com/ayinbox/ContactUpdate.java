package com.ayinbox;

import java.util.List;
import java.util.Map;

/** Body of {@code PATCH /contacts/{id}}. Omitted fields are left unchanged. */
public final class ContactUpdate extends Params<ContactUpdate> {
    public ContactUpdate firstName(String v) { return set("first_name", v); }

    public ContactUpdate lastName(String v) { return set("last_name", v); }

    public ContactUpdate email(String v) { return set("email", v); }

    public ContactUpdate company(String v) { return set("company", v); }

    public ContactUpdate phone(String v) { return set("phone", v); }

    /** Replaces the stored custom fields. */
    public ContactUpdate customFields(Map<String, String> v) { return set("custom_fields", v); }

    public ContactUpdate subscribed(boolean v) { return set("subscribed", v); }

    /** Replaces the contact's campaign list. */
    public ContactUpdate campaigns(List<String> v) { return set("campaigns", v); }

    /** Replaces the contact's labels. */
    public ContactUpdate categories(List<String> v) { return set("categories", v); }

    public ContactUpdate addCategories(List<String> v) { return set("add_categories", v); }

    public ContactUpdate removeCategories(List<String> v) { return set("remove_categories", v); }
}
