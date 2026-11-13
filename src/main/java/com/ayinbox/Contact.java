package com.ayinbox;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

/** A contact. Shapes the API does not fix ({@code lead_score}, {@code campaign_lead}, {@code outreach}) are {@code JsonNode}. */
public class Contact extends Model {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String company;
    private String phone;
    private Map<String, String> customFields;
    private Boolean subscribed;
    private JsonNode campaigns;
    private JsonNode categories;
    private String verificationStatus;
    private String verificationReason;
    private Boolean isCatchAll;
    private String verificationCheckedAt;
    private String mailHost;
    private String espProvider;
    private String consentGivenAt;
    private String consentSource;
    private JsonNode leadScore;
    private JsonNode campaignLead;
    private JsonNode outreach;
    private String createdAt;
    private String updatedAt;

    public String getId() { return id; }

    public String getFirstName() { return firstName; }

    public String getLastName() { return lastName; }

    public String getEmail() { return email; }

    public String getCompany() { return company; }

    public String getPhone() { return phone; }

    public Map<String, String> getCustomFields() { return customFields; }

    public Boolean getSubscribed() { return subscribed; }

    public JsonNode getCampaigns() { return campaigns; }

    public JsonNode getCategories() { return categories; }

    public String getVerificationStatus() { return verificationStatus; }

    public String getVerificationReason() { return verificationReason; }

    public Boolean getCatchAll() { return isCatchAll; }

    public String getVerificationCheckedAt() { return verificationCheckedAt; }

    public String getMailHost() { return mailHost; }

    public String getEspProvider() { return espProvider; }

    public String getConsentGivenAt() { return consentGivenAt; }

    public String getConsentSource() { return consentSource; }

    public JsonNode getLeadScore() { return leadScore; }

    public JsonNode getCampaignLead() { return campaignLead; }

    public JsonNode getOutreach() { return outreach; }

    public String getCreatedAt() { return createdAt; }

    public String getUpdatedAt() { return updatedAt; }
}
