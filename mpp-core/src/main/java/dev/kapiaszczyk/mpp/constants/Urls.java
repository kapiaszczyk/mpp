package dev.kapiaszczyk.mpp.constants;

/**
 * Class containing all URLs used in the application.
 */
public class Urls {

    public static final String AUTH_URL = "/auth";
    public static final String LOGIN_URL = "/login";
    public static final String REGISTER_URL = "/register";
    public static final String REFRESH_TOKEN_URL = "/refresh-token";
    public static final String LOGOUT_URL = "/logout";
    public static final String INTERNAL_SERVICE_TOKEN_URL = "/internal-service/token";

    public static final String ALBUMS_URL_PREFIX = "/albums";

    public static final String PHOTOS_URL_PREFIX = "/photos";

    public static final String PERMISSIONS_URL = "/permissions";

    public static final String REGISTER_ENDPOINT_URL = AUTH_URL + REGISTER_URL;
    public static final String LOGIN_ENDPOINT_URL = AUTH_URL + LOGIN_URL;
    public static final String LOGOUT_ENDPOINT_URL = AUTH_URL + LOGOUT_URL;
    public static final String REFRESH_TOKEN_ENDPOINT_URL = AUTH_URL + REFRESH_TOKEN_URL;
    public static final String INTERNAL_SERVICE_TOKEN_ENDPOINT_URL = AUTH_URL + INTERNAL_SERVICE_TOKEN_URL;

}
