package com.cs.helsinki.fi.interactivesystems;

import android.net.Uri;

public class UrlLibrary {
    public static String SITE_URL = "http://iseteenindus.tootukassa.ee/toopakkumised/";

    private UrlLibrary() {} // prevent instantiation of this class
    
    public static Uri getJobUrl(String id) {
        return Uri.parse(SITE_URL + id);
    }
    
}
