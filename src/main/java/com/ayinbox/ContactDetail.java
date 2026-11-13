package com.ayinbox;

import com.fasterxml.jackson.databind.JsonNode;

/** A contact as {@code GET /contacts/{id}} returns it, with engagement, suppression and source details. */
public final class ContactDetail extends Contact {
    private JsonNode engagement;
    private JsonNode suppression;
    private JsonNode verification;
    private String source;
    private String sourceDetail;
    private String firstSeenAt;
    private String consentCountry;
    private Boolean consentRequired;

    public JsonNode getEngagement() { return engagement; }

    public JsonNode getSuppression() { return suppression; }

    public JsonNode getVerification() { return verification; }

    public String getSource() { return source; }

    public String getSourceDetail() { return sourceDetail; }

    public String getFirstSeenAt() { return firstSeenAt; }

    public String getConsentCountry() { return consentCountry; }

    public Boolean getConsentRequired() { return consentRequired; }
}
