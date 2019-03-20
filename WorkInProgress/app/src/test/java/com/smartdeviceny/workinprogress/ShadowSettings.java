package com.smartdeviceny.workinprogress;



import android.provider.Settings;

import org.robolectric.annotation.Implements;

@Implements(value = Settings.class, callThroughByDefault = true)
public class ShadowSettings {

    private static String COOKIE_DOMAIN = "http://mediation.adnxs.com";
    private static String BASE_URL = "http://mediation.adnxs.com/";
    private static String REQUEST_BASE_URL = "http://mediation.adnxs.com/ut/v2";
    private static String INSTALL_BASE_URL = "http://mediation.adnxs.com/install?";
    public boolean test_mode = true;

    public static String getBaseUrl() {
        return "";
        //return Settings.getSettings().useHttps ? BASE_URL.replace("http:", "https:") : BASE_URL;
    }

    public static String getRequestBaseUrl() {
        return "";
        //return Settings.getSettings().useHttps ? REQUEST_BASE_URL.replace("http:", "https:") : REQUEST_BASE_URL;
    }

    public static String getInstallBaseUrl() {
        return "";//  return Settings.getSettings().useHttps ? INSTALL_BASE_URL.replace("http:", "https:") : INSTALL_BASE_URL;
    }

    public static String getCookieDomain() {
        return COOKIE_DOMAIN;
    }

    public static void setTestURL(String url) {
        BASE_URL = url;
        REQUEST_BASE_URL = url;
        COOKIE_DOMAIN = url;
    }


}