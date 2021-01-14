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

public class Request {

    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private static final String TAG = "Request";
    private static final String AUTHOR = "Author => Menno Spijker";

    private final ConnectionDetector connection;
    private final String uri;

    public Request(ConnectionDetector c, String u){
        connection = c;
        uri = u;
    }

    public String PerformRequest(String kenteken) {
        String result = null;
        String message;
        if (connection.isConnectingToInternet()) {
            try {
                if (kenteken != "" || kenteken != null) {
                    try {
                        URL url = new URL(uri);
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

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
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