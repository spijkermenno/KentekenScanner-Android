package com.MennoSpijker.kentekenscanner.View;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.MennoSpijker.kentekenscanner.ConnectionDetector;
import com.MennoSpijker.kentekenscanner.Factory.KentekenDataFactory;
import com.MennoSpijker.kentekenscanner.Request;
import com.google.android.gms.ads.AdView;

/**
 * Created by Menno on 08/12/2017.
 */

public class Async extends AsyncTask<String, String, String> {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private static final String TAG = "Async";
    private static final String AUTHOR = "Author => Menno Spijker";
    private static final String NAMEFILE = "data.json";

    private LinearLayout linearLayout;
    private Button recent;
    private String kenteken, uri;
    private ScrollView resultView;
    private Context main;
    private ConnectionDetector connection;
    public SearchHandler Khandler;
    public AdView mAdView;
    public Async self;
    public KentekenDataFactory kentekenDataFactory;
    int rounds = 0;
    private String resp;



    public Async(Context m, String k, ScrollView r, String u, ConnectionDetector c, Button re, SearchHandler kh, KentekenDataFactory kdf) {
        try {
            main = m;
            kenteken = k;
            resultView = r;
            uri = u;
            connection = c;
            recent = re;
            Khandler = kh;
            self = this;
            kentekenDataFactory = kdf;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            if (!kenteken.isEmpty()) {
                Request response = new Request(connection, uri);
                resp = response.PerformRequest(kenteken);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resp;
    }

    @Override
    protected void onPostExecute(String result) {
        kentekenDataFactory.addParams(main, resultView, kenteken, Khandler, connection, recent);

        if (result.length() > 3) {
            kentekenDataFactory.fillArray(result);
        }
    }


    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(String... text) {
    }
}