# ayinbox-java

The official Java client for the AY Inbox REST API (Java 11 or newer). Built on the JDK's `java.net.http.HttpClient` and Jackson.

AY Inbox is a hosted cold email platform: it sends cold email at a paced daily volume, brings every reply into one inbox (Unibox), drafts answers from a per-campaign brain, and can offer a meeting time from Cal.com or Calendly. Access is invite only for now: ask at team@ayinbox.com.

AY Inbox is a hosted service. This repository holds the Java client library for its public API. The service itself is not open source.

## Install

Not on Maven Central yet. The coordinates will be `com.ayinbox:ayinbox-java`.

Until then, build it from GitHub as a Gradle composite build. Clone the repository next to your project:

```sh
git clone https://github.com/ayinbox/ayinbox-java.git
```

In your project's `settings.gradle.kts`:

```kotlin
includeBuild("../ayinbox-java")
```

In `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.ayinbox:ayinbox-java:0.1.0")
}
```

Gradle substitutes the included build for the coordinates. With Maven, run `gradle publishToMavenLocal` in the clone and depend on the same coordinates.

## Quick start

```java
import com.ayinbox.*;

public class Main {
    public static void main(String[] args) {
        AyInbox client = AyInbox.builder().build(); // reads AYINBOX_API_KEY

        Identity me = client.me().get();
        System.out.println("Signed in as " + me.getEmail() + " with scopes " + me.getScopes());

        for (Campaign c : client.campaigns().listAll()) {
            System.out.println(c.getStatus() + "  " + c.getName());
        }

        try {
            Contact lee = client.contacts().create(
                    new ContactInput("lee@example.com").firstName("Lee"),
                    RequestOptions.idempotencyKey("import-2026-11-13-lee")).get(0);
            System.out.println("Contact " + lee.getId());
        } catch (PermissionDeniedException e) {
            System.out.println("Missing scope: " + e.getCode() + " (request " + e.getRequestId() + ")");
        }
    }
}
```

Create an API key in the dashboard at https://app.ayinbox.com. Keys start with `aybox_`; OAuth access tokens (`ayat_`) work the same way.

## Client options

```java
AyInbox client = AyInbox.builder()
        .apiKey("aybox_...")                      // default: AYINBOX_API_KEY
        .baseUrl("https://api.ayinbox.com/v1")    // default: AYINBOX_BASE_URL, then this
        .timeout(Duration.ofSeconds(30))          // per attempt
        .maxRetries(2)                            // 0 disables retries
        .httpClient(HttpClient.newHttpClient())   // optional: proxy, executor, TLS
        .build();
```

Every method has an overload taking `RequestOptions` for one call:

```java
RequestOptions.builder()
        .idempotencyKey("create-q4")
        .timeout(Duration.ofSeconds(60))
        .header("X-Request-Id", "my-trace-id")
        .build();
```

The key is never logged or put in an exception message, and `toString()` hides it. The client is thread-safe; create one and share it.

## Resources

| Resource | Methods | Route |
|---|---|---|
| `me()` | `get` | `GET /me` |
| `campaigns()` | `list`, `listAll` | `GET /campaigns` |
| | `get` | `GET /campaigns/{id}` |
| | `create` | `POST /campaigns` |
| | `pause` | `POST /campaigns/{id}/stop` |
| `contacts()` | `list`, `listAll` | `POST /contacts/search` |
| | `get` | `GET /contacts/{id}` |
| | `create` | `POST /contacts` |
| | `update` | `PATCH /contacts/{id}` |
| | `delete` | `DELETE /contacts/{id}` |
| `mailboxes()` | `list`, `listAll` | `GET /emails` |
| `unibox()` | `listThreads`, `listAllThreads` | `GET /unibox` |
| | `getThread`, `getThreadAll` | `GET /unibox/thread` |
| `webhooks()` / `WebhookSignature` | `verify`, `constructEvent` | local, no HTTP |

Parameter objects (`CampaignListParams`, `CampaignCreateParams`, `ContactSearch`, `ContactInput`, `ContactUpdate`, `MailboxListParams`, `ThreadListParams`, `ThreadParams`) have typed setters for common fields and `set(key, value)` for any other JSON field. `set("end_date", null)` sends an explicit JSON `null`.

Every other route is reachable through `request`:

```java
JsonNode count = client.request("GET", "/unibox/count");
JsonNode steps = client.request("GET", "/campaigns/" + id + "/steps");
JsonNode added = client.request("POST", "/suppressions", null,
        Map.of("entries", List.of(Map.of("value", "example.org"))),
        RequestOptions.idempotencyKey("suppress-example-org"));
```

`request` returns a Jackson `JsonNode` (`NullNode` for an empty body). `requestRaw` returns status, headers and bytes.

Response models never fail on unknown fields: fields without a typed getter are kept in `getAdditional()` and readable with `get("field")`, so new API fields never break deserialization. Timestamps are RFC 3339 strings. Fields whose shape the API does not fix (lead scores, engagement, schedule windows, webhook `data`) are `JsonNode`. `Page.getRaw()` keeps the whole list response, including extras such as `counts` on contact search.

## Errors

Every API error is an `ApiException` with `getStatus()`, `getCode()`, `getApiMessage()`, `getReason()`, `getRequestId()`, `getBody()`, `getHeaders()` and `getRetryAfterSeconds()`. Branch on status and code, never on the message.

| Exception | Status |
|---|---|
| `BadRequestException` (a `ValidationException`) | 400 |
| `AuthenticationException` | 401 |
| `PaymentRequiredException` | 402 |
| `PermissionDeniedException` | 403 |
| `NotFoundException` | 404 |
| `ConflictException` | 409 |
| `UnprocessableException` (a `ValidationException`) | 422 |
| `RateLimitException` | 429 |
| `InternalServerException` (a `ServerException`) | 500 and other 5xx |
| `ServiceUnavailableException` (a `ServerException`) | 503 |
| `ApiConnectionException`, `ApiTimeoutException` (both `NetworkException`) | no response |

All of them extend `AyInboxException`, an unchecked exception. A 429 from a per-user limiter carries no `code` in its body; the client still reports `rate_limit_exceeded` and reads the wait from `Retry-After`, `retry_after` or `retry_after_ms`.

## Retries and timeouts

The client retries 429, 502, 503, 504 and network failures up to `maxRetries` times (default 2). It waits for `Retry-After` when the API sends it (capped at 120 seconds), otherwise exponential backoff with full jitter (0.5 s base, 8 s cap).

A `POST` or `PATCH` without an idempotency key is retried only on 429 (which the API never runs) and on a connection that was never made. Pass an idempotency key to let it retry other failures. `contacts().list` is a read sent as a `POST` and is retried like a `GET`. A 500 and `409 idempotency_outcome_unknown` are never retried.

The default timeout is 30 seconds per attempt. Override it per client or per call with `RequestOptions.builder().timeout(...)`.

## Pagination

List methods return one `Page<T>` with `getData()`, `getNextCursor()`, `hasMore()` and `getTotal()` (not on every list). The `listAll` variants return an `AutoPager<T>`, an `Iterable` with `stream()`, that fetches pages lazily and follows `next_cursor` until `has_more` is false:

```java
client.contacts()
        .listAll(new ContactSearch().campaignIds(List.of(campaignId)))
        .stream()
        .forEach(c -> System.out.println(c.getEmail()));
```

For any other paginated route, `client.paginate("/suppressions", null)` yields each row as a `JsonNode`. Recommended page sizes are 1 to 100.

## Idempotency

Every mutating call accepts `RequestOptions.idempotencyKey("...")`, sent as `Idempotency-Key` (1 to 255 visible ASCII characters). The API stores the result for 24 hours: the same key and request replays the original answer, and the same key with a different request answers `409 conflict`. If the first attempt ended without an outcome, the key answers `409 idempotency_outcome_unknown` for 24 hours: find out whether the change happened before trying again with a new key. The client does not generate keys for you; every retry of a call reuses the key you gave it.

## Webhooks

```java
boolean ok = WebhookSignature.verify(
        rawBodyBytes,                               // the raw request body, before JSON parsing
        request.getHeader("X-Ayinbox-Signature"),
        "whsec_...");                               // the endpoint secret, prefix included

// or verify and parse in one step, throwing SignatureVerificationException:
WebhookEvent event = WebhookSignature.constructEvent(rawBodyBytes, header, secret);
System.out.println(event.getEventType() + " " + event.getId());
```

The signature is HMAC-SHA256 over `"<t>.<raw body>"`, compared in constant time with `MessageDigest.isEqual`. A timestamp more than 300 seconds from now fails by default; overloads take a tolerance and the current time. Use the `X-Ayinbox-Event-Id` header (equal to `event.getId()`) to drop retried deliveries.

## Calls that send email

These calls put real email on the wire:

- `campaigns.start` (`POST /campaigns/{id}/start`, through `request`): needs `START_CAMPAIGNS` or `SEND_CAMPAIGNS`.
- `campaigns.update` with `status: "active"` (and `completed` or `paused_undeliverable`), through `request`: needs `START_CAMPAIGNS` or `SEND_CAMPAIGNS` in addition to `WRITE_CAMPAIGNS`.
- `campaigns.leads.resume` on a finished campaign (`POST /campaigns/{id}/leads/{contactId}/resume`, through `request`): needs `START_CAMPAIGNS` or `SEND_CAMPAIGNS`.
- `unibox.reply` and `unibox.approveAgentDraft` (`POST /unibox/reply`, `POST /unibox/agent-drafts/{id}/approve`, through `request`): need `WRITE_UNIBOX`. A key with `WRITE_UNIBOX` can send replies from any mailbox it can reach.
- Also through `request`: `POST /unibox/compose`, `POST /emails/{id}/send`, `POST /campaigns/{id}/test-email`, the first-email review queue (`/campaigns/{id}/reviews/...`) and placement tests need `SEND_CAMPAIGNS` (compose needs `WRITE_UNIBOX`).

Adding contacts can send mail without calling start. Contacts added to an `active` campaign (through `contacts().create` with `campaigns`) are queued; a `completed` campaign starts again on its own when leads are added; a contact that comes to match a segment linked to a sending campaign is enrolled.

`START_CAMPAIGNS` starts, resumes and pauses campaigns and nothing else. `SEND_CAMPAIGNS` covers everything `START_CAMPAIGNS` covers plus the other sending actions. `WRITE_CAMPAIGNS` alone edits drafts and pauses, and cannot start anything. `campaigns().create` never sends and `campaigns().pause` sends nothing.

A key for a model that reads the inbox should use the "Agent: read and draft" preset (84574239): every read scope plus `AI_DRAFT`, with nothing that writes or sends.

Some calls spend workspace credits (AI drafts, research, enrichment, paid tools). They answer `402 insufficient_credits` when the balance is out.

Retrying a send without an idempotency key can send twice. Pass an idempotency key on every mutating call you might retry.

## Examples

`examples/` holds two programs: `gradle listCampaigns` (needs `AYINBOX_API_KEY`) and `gradle verifyWebhook` (runs offline against the test vector).

## Development

Tests run against a local `com.sun.net.httpserver.HttpServer` fed from `src/test/resources/fixtures`, with no network and no real key:

```sh
gradle check
```

## Links

- API reference: https://docs.ayinbox.com/api/
- Dashboard: https://app.ayinbox.com
- Contact: team@ayinbox.com

## License

MIT. See `LICENSE`.
