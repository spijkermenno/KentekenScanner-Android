package com.MennoSpijker.kentekenscanner;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Menno on 08/12/2017.
 */

public class APIHelper {

    private static final String TAG = "APIHelper";

    private final ConnectionDetector connection;
    private final String uri;

    public APIHelper(ConnectionDetector connectionDetector, String uri){
        connection = connectionDetector;
        this.uri = uri;
    }

    public String run(String kenteken) {
        String result = null;
        if (connection.isConnectingToInternet()) {
            try {
                if (!kenteken.equals("")) {
                    try {
                        URL url = new URL(uri);
                        Log.d(TAG, url.toString());
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        try {
                            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                            result = readStream(in);
                        } catch (IOException E) {
                            E.printStackTrace();
                        } finally {
                            urlConnection.disconnect();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    result = "ERROR";
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
            return result;
        } else {
            return "No internet connection.";
        }
    }

    private String readStream(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException i) {
                i.printStackTrace();
            }
        }
        return sb.toString();
    }
}