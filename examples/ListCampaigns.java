import com.ayinbox.AyInbox;
import com.ayinbox.AyInboxException;
import com.ayinbox.Campaign;

/** Lists every campaign. Run with AYINBOX_API_KEY set: gradle listCampaigns */
public final class ListCampaigns {
    private ListCampaigns() {}

    public static void main(String[] args) {
        AyInbox client = AyInbox.builder().build();
        try {
            System.out.println("Signed in as " + client.me().get().getEmail());
            for (Campaign c : client.campaigns().listAll()) {
                System.out.println(c.getStatus() + "  " + c.getName() + "  " + c.getId());
            }
        } catch (AyInboxException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
