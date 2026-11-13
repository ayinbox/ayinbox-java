package com.ayinbox;

import java.util.List;

/** The credential's identity, from {@code GET /me}. */
public final class Identity extends Model {
    private String userId;
    private String email;
    private String name;
    private String firstName;
    private String lastName;
    private String organizationId;
    private String organizationName;
    private String authType;
    private List<String> scopes;

    public String getUserId() { return userId; }

    public String getEmail() { return email; }

    public String getName() { return name; }

    public String getFirstName() { return firstName; }

    public String getLastName() { return lastName; }

    public String getOrganizationId() { return organizationId; }

    public String getOrganizationName() { return organizationName; }

    public String getAuthType() { return authType; }

    public List<String> getScopes() { return scopes; }
}
