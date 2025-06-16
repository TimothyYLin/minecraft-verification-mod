package name.modid;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HttpApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpApiClient.class);
    static HttpClient client = HttpClient.newHttpClient();

    public static CompletableFuture<Boolean> isVerifiedAsync(String playerUuid){
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(AppConfig.getPortalApiUrl() + "/is-verified?uuid=" + playerUuid))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        int statusCode = response.statusCode();
                        if(statusCode == 200){
                            Gson gson = new Gson();
                            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();

                            Map<String, Object> responseMap = gson.fromJson(response.body(), mapType);
                            Object verifiedValue = responseMap.get("verified");
                            if(verifiedValue instanceof Boolean){
                                return (Boolean) verifiedValue;
                            }
                            LOGGER.warn("Received 200 OK but the 'verified' field was missing or not a boolean. " +
                                    "Body: {}", response.body());
                            return false;
                        }else if (statusCode == 404) {
                            LOGGER.info("Player with UUID {} is not yet linked in the database " +
                                    "(API returned 404 Not Found).", playerUuid);
                        }
                        else if (statusCode == 400) {
                            LOGGER.warn("The server reported a Bad Request (400). This may indicate a bug " +
                                    "in the mod's API call. Body: {}", response.body());
                        }
                        else {
                            LOGGER.error("API returned an unexpected status code: {}. " +
                                    "Body: {}", statusCode, response.body());
                        }
                        return false;
            }).exceptionally(error -> {
                LOGGER.error("API call to is-verified failed:", error);
                return false;
            });
        } catch (URISyntaxException e) {
            LOGGER.error("Malformed URI for is-verified check:", e);
            return CompletableFuture.completedFuture(false);
        }
    }
}
