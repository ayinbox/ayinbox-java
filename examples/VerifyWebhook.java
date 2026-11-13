import com.ayinbox.WebhookEvent;
import com.ayinbox.WebhookSignature;
import java.nio.charset.StandardCharsets;

/** Verifies the documented test delivery offline: gradle verifyWebhook */
public final class VerifyWebhook {
    private VerifyWebhook() {}

    public static void main(String[] args) {
        String secret = "whsec_test_secret";
        long timestamp = 1700000000L;
        byte[] body = ("{\"id\":\"00000000-0000-4000-8000-000000000001\",\"event_type\":\"campaign.reply_received\","
                + "\"organization_id\":\"00000000-0000-4000-8000-0000000000aa\",\"created_at\":\"2023-11-14T22:13:20Z\",\"data\":{}}")
                .getBytes(StandardCharsets.UTF_8);
        String header = "t=1700000000,v1=d1efe3010ae2523566985158ab4852f8f3569365a06941ef87e3a641e3171342";

        // In a real handler, pass the raw request body and the current time (the 3-argument overload).
        WebhookEvent event = WebhookSignature.constructEvent(body, header, secret, 300, timestamp);
        System.out.println("verified " + event.getEventType() + " " + event.getId());

        boolean tampered = WebhookSignature.verify("{}".getBytes(StandardCharsets.UTF_8), header, secret, 300, timestamp);
        System.out.println("tampered body verifies: " + tampered);
    }
}
