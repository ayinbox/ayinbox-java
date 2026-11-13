package com.ayinbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ClientTest {
    private static final String KEY = "aybox_test_0000000000000000000000000000000000000000000";

    private MockServer server;
    private AyInbox client;

    @BeforeEach
    void setUp() {
        server = new MockServer();
        client = client(2);
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    private AyInbox client(int retries) {
        return AyInbox.builder().apiKey(KEY).baseUrl(server.baseUrl() + "/").maxRetries(retries).build();
    }

    private static JsonNode json(String s) throws Exception {
        return Json.MAPPER.readTree(s);
    }

    @Test
    void sendsAuthUserAgentAndAcceptAndParsesMe() {
        server.fixture(200, "me.json", "X-Request-Id", "req-fixture-0001");
        Identity me = client.me().get();

        MockServer.Recorded r = server.last();
        assertEquals("GET", r.method);
        assertEquals("/v1/me", r.path);
        assertEquals("Bearer " + KEY, r.header("Authorization"));
        assertEquals(1, r.headerCount("Authorization"));
        assertEquals("ayinbox-java/" + AyInbox.VERSION, r.header("User-Agent"));
        assertEquals("application/json", r.header("Accept"));
        assertNull(r.header("Idempotency-Key"));

        assertEquals("dana@example.com", me.getEmail());
        assertEquals("api_key", me.getAuthType());
        assertEquals(Arrays.asList("read_campaigns", "read_contacts", "write_contacts"), me.getScopes());
        assertEquals("00000000-0000-4000-8000-0000000000aa", me.getOrganizationId());
    }

    @Test
    void neverShowsTheKey() {
        assertFalse(client.toString().contains(KEY));
        server.fixture(401, "errors/401_unauthorized.json");
        AuthenticationException e = assertThrows(AuthenticationException.class, () -> client.me().get());
        assertFalse(e.getMessage().contains(KEY));
    }

    @Test
    void missingKeyIsRefusedUpFront() {
        if (System.getenv("AYINBOX_API_KEY") != null) return;
        assertThrows(AyInboxException.class, () -> AyInbox.builder().build());
    }

    @ParameterizedTest
    @CsvSource({
        "400_bad_request.json, 400, bad_request, BadRequestException",
        "400_invalid_cursor.json, 400, bad_request, BadRequestException",
        "401_unauthorized.json, 401, unauthorized, AuthenticationException",
        "401_api_key_holder_left.json, 401, api_key_holder_left, AuthenticationException",
        "402_insufficient_credits.json, 402, insufficient_credits, PaymentRequiredException",
        "403_forbidden.json, 403, forbidden, PermissionDeniedException",
        "403_api_key_mailbox_limited.json, 403, api_key_mailbox_limited, PermissionDeniedException",
        "404_not_found.json, 404, not_found, NotFoundException",
        "409_conflict.json, 409, conflict, ConflictException",
        "409_idempotency_outcome_unknown.json, 409, idempotency_outcome_unknown, ConflictException",
        "409_idempotent_secret_withheld.json, 409, idempotent_secret_withheld, ConflictException",
        "422_unprocessable.json, 422, unprocessable, UnprocessableException",
        "429_api_key_rate_limited.json, 429, rate_limit_exceeded, RateLimitException",
        "500_internal_error.json, 500, internal_error, InternalServerException",
        "503_service_unavailable.json, 503, service_unavailable, ServiceUnavailableException",
    })
    void mapsEachErrorEnvelope(String file, int status, String code, String type) {
        AyInbox c = client(0);
        server.fixture(status, "errors/" + file);
        ApiException e = assertThrows(ApiException.class, () -> c.campaigns().get("11111111-1111-4111-8111-111111111111"));
        assertEquals(type, e.getClass().getSimpleName());
        assertEquals(status, e.getStatus());
        assertEquals(code, e.getCode());
        assertEquals("req-fixture-0001", e.getRequestId());
        assertFalse(e.getApiMessage().isEmpty());
        if (status == 400 || status == 422) assertInstanceOf(ValidationException.class, e);
        if (status >= 500) assertInstanceOf(ServerException.class, e);
        if (status == 429) assertEquals(12L, e.getRetryAfterSeconds());
    }

    @Test
    void categoryRateLimitWithoutCodeStillMaps() {
        AyInbox c = client(0);
        server.fixture(429, "errors/429_category_rate_limited.json", "X-Request-Id", "req-header-1");
        RateLimitException e = assertThrows(RateLimitException.class, () -> c.me().get());
        assertEquals("rate_limit_exceeded", e.getCode());
        assertEquals(12L, e.getRetryAfterSeconds());
        assertEquals("req-header-1", e.getRequestId());
        assertEquals("Rate limit exceeded for write operations", e.getApiMessage());
    }

    @Test
    void retryAfterHeaderWinsOverBody() {
        AyInbox c = client(0);
        server.fixture(429, "errors/429_category_rate_limited.json", "Retry-After", "3");
        RateLimitException e = assertThrows(RateLimitException.class, () -> c.me().get());
        assertEquals(3L, e.getRetryAfterSeconds());
    }

    @Test
    void nonJsonErrorKeepsTextAndDefaultCode() {
        AyInbox c = client(0);
        server.enqueue(502, "<html>bad gateway</html>");
        InternalServerException e = assertThrows(InternalServerException.class, () -> c.me().get());
        assertEquals("internal_error", e.getCode());
        assertEquals("<html>bad gateway</html>", e.getBody());
    }

    @Test
    void retries503ThenSucceeds() {
        server.fixture(503, "errors/503_service_unavailable.json", "Retry-After", "0");
        server.fixture(200, "me.json");
        assertEquals("dana@example.com", client.me().get().getEmail());
        assertEquals(2, server.requests.size());
    }

    @Test
    void retries429WithRetryAfterEvenOnKeylessPost() {
        server.fixture(429, "errors/429_api_key_rate_limited.json", "Retry-After", "0");
        server.fixture(200, "campaign.json");
        Campaign c = client.campaigns().create(new CampaignCreateParams("Q4 outreach"));
        assertEquals("Q4 outreach", c.getName());
        assertEquals(2, server.requests.size());
    }

    @Test
    void doesNotRetryKeylessPostOn503() {
        server.fixture(503, "errors/503_service_unavailable.json", "Retry-After", "0");
        assertThrows(ServiceUnavailableException.class,
                () -> client.campaigns().create(new CampaignCreateParams("Q4 outreach")));
        assertEquals(1, server.requests.size());
    }

    @Test
    void retriesPostWithIdempotencyKeyAndReusesTheKey() {
        server.fixture(503, "errors/503_service_unavailable.json", "Retry-After", "0");
        server.fixture(200, "contacts_create.json");
        client.contacts().create(new ContactInput("lee@example.com"), RequestOptions.idempotencyKey("import-lee-1"));
        assertEquals(2, server.requests.size());
        assertEquals("import-lee-1", server.requests.get(0).header("Idempotency-Key"));
        assertEquals("import-lee-1", server.requests.get(1).header("Idempotency-Key"));
    }

    @Test
    void neverRetriesOutcomeUnknownOr500() {
        server.fixture(409, "errors/409_idempotency_outcome_unknown.json");
        assertThrows(ConflictException.class,
                () -> client.contacts().create(new ContactInput("a@example.com"), RequestOptions.idempotencyKey("k")));
        server.fixture(500, "errors/500_internal_error.json");
        assertThrows(InternalServerException.class, () -> client.me().get());
        assertEquals(2, server.requests.size());
    }

    @Test
    void contactSearchIsRetriedLikeARead() {
        server.fixture(503, "errors/503_service_unavailable.json", "Retry-After", "0");
        server.fixture(200, "contacts_list.json");
        Page<Contact> page = client.contacts().list(new ContactSearch().query("lee"));
        assertFalse(page.getData().isEmpty());
        assertEquals(2, server.requests.size());
    }

    @Test
    void givesUpAfterMaxRetries() {
        AyInbox c = client(1);
        server.fixture(503, "errors/503_service_unavailable.json", "Retry-After", "0");
        server.fixture(503, "errors/503_service_unavailable.json", "Retry-After", "0");
        assertThrows(ServiceUnavailableException.class, () -> c.me().get());
        assertEquals(2, server.requests.size());
    }

    @Test
    void timeoutIsATypedNetworkError() {
        AyInbox c = AyInbox.builder().apiKey(KEY).baseUrl(server.baseUrl()).maxRetries(0)
                .timeout(Duration.ofMillis(200)).build();
        server.enqueueDelayed(1500, 200, Fixtures.read("me.json"));
        assertThrows(ApiTimeoutException.class, () -> c.me().get());
    }

    @Test
    void connectionFailureIsATypedNetworkError() {
        String url = server.baseUrl();
        server.close();
        AyInbox c = AyInbox.builder().apiKey(KEY).baseUrl(url).maxRetries(0).build();
        assertThrows(NetworkException.class, () -> c.me().get());
    }

    @Test
    void autoPagesAcrossTwoPages() {
        server.fixture(200, "campaigns_list.json");
        server.fixture(200, "campaigns_list_page2.json");
        List<Campaign> all = client.campaigns().listAll(new CampaignListParams().limit(2)).stream()
                .collect(Collectors.toList());

        assertEquals(2, server.requests.size());
        assertEquals("limit=2", server.requests.get(0).query);
        assertTrue(server.requests.get(1).query.contains("cursor=c1_ZmFrZS1jdXJzb3ItcGFnZS0y"));
        assertTrue(server.requests.get(1).query.contains("limit=2"));
        assertTrue(all.size() >= 2);
        assertNotNull(all.get(0).getId());
    }

    @Test
    void contactAutoPagerResendsTheSameBody() {
        server.fixture(200, "campaign_leads_list.json");
        String second = "{\"data\":[],\"pagination\":{\"next_cursor\":null,\"has_more\":false}}";
        server.enqueue(200, second);
        List<Contact> rows = new ArrayList<>();
        for (Contact c : client.contacts().listAll(
                new ContactSearch().campaignIds(Collections.singletonList("11111111-1111-4111-8111-222222222222")).limit(1))) {
            rows.add(c);
        }
        assertFalse(rows.isEmpty());
        if (server.requests.size() == 2) {
            assertEquals(server.requests.get(0).body, server.requests.get(1).body);
        }
        assertTrue(server.requests.get(0).body.contains("\"campaign_ids\""));
        assertNotNull(rows.get(0).getCampaignLead());
    }

    @Test
    void paginateWorksOnAnyRoute() {
        server.fixture(200, "suppressions_list.json");
        int n = 0;
        for (JsonNode row : client.paginate("/suppressions", null)) {
            assertNotNull(row.get("id"));
            n++;
        }
        assertTrue(n > 0);
        assertEquals("/v1/suppressions", server.last().path);
    }

    @Test
    void genericRequestSendsBodyAndHeaders() throws Exception {
        server.fixture(200, "suppressions_add.json");
        Map<String, Object> body = Collections.singletonMap("entries",
                Collections.singletonList(Collections.singletonMap("value", "example.org")));
        JsonNode out = client.request("POST", "/suppressions", null, body,
                RequestOptions.builder().idempotencyKey("suppress-1").header("X-Request-Id", "my-req-1").build());
        assertTrue(out.has("added"));
        MockServer.Recorded r = server.last();
        assertEquals("suppress-1", r.header("Idempotency-Key"));
        assertEquals("my-req-1", r.header("X-Request-Id"));
        assertEquals("application/json", r.header("Content-Type"));
        assertEquals(json("{\"entries\":[{\"value\":\"example.org\"}]}"), json(r.body));
    }

    @Test
    void emptyBodyIsNullNode() {
        server.enqueue(204, null);
        assertTrue(client.request("DELETE", "/campaigns/11111111-1111-4111-8111-111111111111").isNull());
    }

    // Happy paths, one or more per resource.

    @Test
    void campaignsListGetCreatePause() throws Exception {
        server.fixture(200, "campaigns_list.json");
        Page<Campaign> page = client.campaigns().list(new CampaignListParams().status("active").q("Q4"));
        assertTrue(page.hasMore());
        assertEquals("c1_ZmFrZS1jdXJzb3ItcGFnZS0y", page.getNextCursor());
        assertTrue(server.last().query.contains("status=active"));

        server.fixture(200, "campaign.json");
        Campaign c = client.campaigns().get("11111111-1111-4111-8111-111111111111");
        assertEquals("/v1/campaigns/11111111-1111-4111-8111-111111111111", server.last().path);
        assertNotNull(c.getStatus());
        assertNotNull(c.getCreatedAt());

        server.fixture(200, "campaign.json");
        client.campaigns().create(new CampaignCreateParams("Q4 outreach").dailyLimit(40).set("start_date", null),
                RequestOptions.idempotencyKey("create-q4"));
        MockServer.Recorded r = server.last();
        assertEquals("POST", r.method);
        assertEquals("create-q4", r.header("Idempotency-Key"));
        assertEquals(json("{\"name\":\"Q4 outreach\",\"daily_limit\":40,\"start_date\":null}"), json(r.body));

        server.fixture(200, "campaign_stop.json");
        JsonNode stop = client.campaigns().pause("11111111-1111-4111-8111-111111111111");
        assertEquals("stopped", stop.get("status").asText());
        assertEquals("/v1/campaigns/11111111-1111-4111-8111-111111111111/stop", server.last().path);
    }

    @Test
    void contactsListGetCreateUpdateDelete() throws Exception {
        server.fixture(200, "contacts_list.json");
        Page<Contact> page = client.contacts().list(new ContactSearch().subscribed(true).limit(25));
        MockServer.Recorded r = server.last();
        assertEquals("POST", r.method);
        assertEquals("/v1/contacts/search", r.path);
        assertEquals("limit=25", r.query);
        assertEquals(json("{\"subscribed\":true}"), json(r.body));
        assertFalse(page.getData().isEmpty());
        assertNotNull(page.getRaw().get("pagination"));

        server.fixture(200, "contact.json");
        ContactDetail d = client.contacts().get("22222222-2222-4222-8222-111111111111");
        assertNotNull(d.getEmail());

        server.fixture(200, "contacts_create.json");
        List<Contact> created = client.contacts().create(Arrays.asList(
                new ContactInput("lee@example.com").firstName("Lee"),
                new ContactInput("sam@example.com")), RequestOptions.idempotencyKey("batch-1"));
        assertEquals("lee@example.com", created.get(0).getEmail());
        assertEquals("Head of Ops", created.get(0).getCustomFields().get("title"));
        assertTrue(json(server.last().body).isArray());

        server.fixture(200, "contact.json");
        client.contacts().update("22222222-2222-4222-8222-111111111111", new ContactUpdate().company("New Co"));
        assertEquals("PATCH", server.last().method);
        assertEquals(json("{\"company\":\"New Co\"}"), json(server.last().body));

        server.enqueue(204, null);
        client.contacts().delete("22222222-2222-4222-8222-111111111111");
        assertEquals("DELETE", server.last().method);
    }

    @Test
    void mailboxesList() {
        server.fixture(200, "mailboxes_list.json");
        Page<Mailbox> page = client.mailboxes().list(new MailboxListParams().limit(10));
        assertEquals("/v1/emails", server.last().path);
        assertFalse(page.getData().isEmpty());
        assertNotNull(page.getData().get(0).getEmail());
    }

    @Test
    void uniboxListThreadsAndGetThread() {
        server.fixture(200, "unibox_threads.json");
        Page<MessagePreview> threads = client.unibox().listThreads(new ThreadListParams().unseen(true));
        assertTrue(server.last().query.contains("unseen=true"));
        MessagePreview row = threads.getData().get(0);
        assertEquals("thread-example-001", row.getThreadId());
        assertEquals(Integer.valueOf(2), row.getMessageCount());

        server.fixture(200, "unibox_thread.json");
        Page<MessagePreview> thread = client.unibox().getThread("thread-example-001");
        assertEquals("/v1/unibox/thread", server.last().path);
        assertEquals("thread_id=thread-example-001", server.last().query);
        assertFalse(thread.hasMore());
        assertNull(thread.getNextCursor());
    }

    @Test
    void unknownFieldsAreKept() {
        server.enqueue(200, "{\"user_id\":\"u\",\"email\":\"a@example.com\",\"brand_new\":{\"x\":1}}");
        Identity me = client.me().get();
        assertEquals(1, me.get("brand_new").get("x").asInt());
    }
}
