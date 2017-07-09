package com.neuandroid.xkcd;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by jienanzhang on 08/07/2017.
 */

public class NetworkUtils {

    public static final String XKCD_QUERY_BASE_URL = "https://xkcd.com/info.0.json";
    public static final String XKCD_QUERY_BY_ID_URL = "https://xkcd.com/%s/info.0.json";

    private final static String TAG = "NetworkUtils";


    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
