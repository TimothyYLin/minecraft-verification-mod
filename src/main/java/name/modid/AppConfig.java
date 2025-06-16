package name.modid;

public class AppConfig {
    public static String getApiKey(){
        return System.getenv("INTERNAL_API_KEY");
    }

    public static String getPortalApiUrl(){
        return System.getenv("PORTAL_API_URL");
    }
}
