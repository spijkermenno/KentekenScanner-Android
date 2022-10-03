package com.MennoSpijker.kentekenscanner.View;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import com.MennoSpijker.kentekenscanner.APIHelper;
import com.MennoSpijker.kentekenscanner.ConnectionDetector;
import com.MennoSpijker.kentekenscanner.Factory.LicencePlateDataFactory;

/**
 * Created by Menno on 08/12/2017.
 */

public class Async extends AsyncTask<String, String, String> {
    private static final String TAG = "Async";

    private String kenteken, uri;
    private ScrollView resultView;
    private MainActivity mainActivity;
    private ConnectionDetector connection;
    public KentekenHandler Khandler;
    public LicencePlateDataFactory licencePlateDataFactory;
    private String resp;
    private ProgressBar progressBar;

    public Async(MainActivity mainActivity, String k, ScrollView r, String u, ConnectionDetector c, KentekenHandler kh, LicencePlateDataFactory kdf, ProgressBar progressBar) {
        try {
            this.mainActivity = mainActivity;
            kenteken = k;
            resultView = r;
            uri = u;
            connection = c;
            Khandler = kh;
            licencePlateDataFactory = kdf;
            this.progressBar = progressBar;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String doInBackground(String... params) {
        progressBar.setProgress(60);
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
        licencePlateDataFactory.addParams(mainActivity, resultView, kenteken, Khandler);
        Log.d(TAG, "onPostExecute: " + result);
        licencePlateDataFactory.fillArray(result, progressBar);
    }


    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(String... text) {
    }
}