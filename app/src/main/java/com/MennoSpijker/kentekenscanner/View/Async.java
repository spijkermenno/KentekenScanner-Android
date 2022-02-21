package com.MennoSpijker.kentekenscanner.View;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ScrollView;

import com.MennoSpijker.kentekenscanner.APIHelper;
import com.MennoSpijker.kentekenscanner.ConnectionDetector;
import com.MennoSpijker.kentekenscanner.Factory.KentekenDataFactory;

/**
 * Created by Menno on 08/12/2017.
 */

public class Async extends AsyncTask<String, String, String> {
    private static final String TAG = "Async";

    private String kenteken, uri;
    private ScrollView resultView;
    private Context main;
    private ConnectionDetector connection;
    public KentekenHandler Khandler;
    public KentekenDataFactory kentekenDataFactory;
    private String resp;



    public Async(Context m, String k, ScrollView r, String u, ConnectionDetector c, KentekenHandler kh, KentekenDataFactory kdf) {
        try {
            main = m;
            kenteken = k;
            resultView = r;
            uri = u;
            connection = c;
            Khandler = kh;
            kentekenDataFactory = kdf;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            if (!kenteken.isEmpty()) {
                APIHelper response = new APIHelper(connection, uri);
                resp = response.run(kenteken);
                Log.d(TAG, "doInBackground: " + resp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resp;
    }

    @Override
    protected void onPostExecute(String result) {
        kentekenDataFactory.addParams(main, resultView, kenteken, Khandler);
        Log.d(TAG, "onPostExecute: " + result);
        kentekenDataFactory.fillArray(result);
    }


    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(String... text) {
    }
}