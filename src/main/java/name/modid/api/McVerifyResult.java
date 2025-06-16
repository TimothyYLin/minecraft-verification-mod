package name.modid.api;

public record McVerifyResult(Status status, String responseBody) {
    public enum Status{
        SUCCESS,
        BAD_REQUEST,
        INVALID_CODE,
        CONFLICT,
        EXPIRED_CODE,
        INTERNAL_SERVER_ERROR,
        API_ERROR
    }
}
