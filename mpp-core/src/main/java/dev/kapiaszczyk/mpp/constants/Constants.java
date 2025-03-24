package dev.kapiaszczyk.mpp.constants;

/**
 * Constants used in the application.
 */
public class Constants {

    public static final String ROLE_CLAIM = "roles";
    public static final String ACCESS_TOKEN_HEADER = "accessToken";
    public static final String REFRESH_TOKEN_HEADER = "refreshToken";

    public static final String ISSUER = "MiniPhotoPlatform";
    public static final String AUDIENCE = "MiniPhotoPlatform";

    public static final long ACCESS_TOKEN_EXPIRATION = 6 * 15 * 60; // TODO: Temporary value
    public static final long REFRESH_TOKEN_EXPIRATION = 6 * 60 * 60;

    /**
     * Allows creating albums within albums, that are not root.
     */
    public static final boolean NESTING_ALBUMS_ALLOWED = false;

    public static final String[] SUPPORTED_FORMATS = new String[]{
            "jpeg",
            "png",
            "webp",
            "gif",
            "jpg"
    };


    /**
     * Service token expiration time in seconds - 2 hours
     */
    public static final long SERVICE_TOKEN_EXPIRATION = 2 * 60 * 60;

    public static final String TAG_SERVICE_NAME = "mpp-tagger";

    public static final String RABBIT_REQUEST_QUEUE = "photo.tagging.request";
    public static final String RABBIT_RESPONSE_QUEUE = "photo.tagging.response";

    public static final String API_NAME = "MiniPhotoPlatform API";
    public static final String API_DESCRIPTION = "API for managing photos and albums of MiniPhotoPlatform";
    public static final String API_VERSION = "1.0";

    public static final String TAGGING_RESPONSE_PHOTO_KEY = "photoId";
    public static final String TAGGING_RESPONSE_TAG_KEY = "tag";
}
