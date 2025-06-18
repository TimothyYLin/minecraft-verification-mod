package name.modid;

public class AppConfig {
    public static String getApiKey(){
        String apiKey = System.getenv("INTERNAL_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("FATAL: INTERNAL_API_KEY environment variable is not set!");
        }
        return apiKey;
    }

    public static String getPortalApiUrl(){
        String url = System.getenv("PORTAL_API_URL");
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("FATAL: PORTAL_API_URL environment variable is not set!");
        }
        return url;
    }

    public static final String IS_VERIFIED_ENDPOINT = "/is-verified";
    public static final String MC_VERIFY_ENDPOINT = "/internal/mc-verify";
}
