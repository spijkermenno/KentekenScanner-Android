package com.y_gap.menno.kentekenscanner;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.DataFormatException;


public class Scan extends Activity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private static final String TAG = "Author => Menno Spijker";
    public Button button;
    public ScrollView result;
    public ProgressBar progressBar;
    public Async runner;
    public Request response;
    public EditText text;
    public String kenteken;
    public String uri;
    public URL url;
    private ConnectionDetector connection;

    final String BUILD = "B0.2";

    ArrayList<String> shownKeys = new ArrayList<String>();
    ArrayList<String> resultList = new ArrayList<String>();

    private static AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        shownKeys.add("kenteken");
        shownKeys.add("merk");
        shownKeys.add("eerste_kleur");
        shownKeys.add("tweede_kleur");
        shownKeys.add("handelsbenaming");
        shownKeys.add("inrichting");
        shownKeys.add("openstaande_terugroepactie_indicator");
        shownKeys.add("vervaldatum_apk");
        shownKeys.add("wacht_op_keuren");
        shownKeys.add("zuinigheidslabel");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        button = findViewById(R.id.sendRequest);
        result = findViewById(R.id.scroll);
        progressBar = findViewById(R.id.progressBar);
        final EditText text = findViewById(R.id.kenteken);
        connection = new ConnectionDetector(this);

        try {
            MobileAds.initialize(this, "ca-app-pub-4928043878967484~7828914059");
            //MobileAds.initialize(this, "ca-app-pub-3940256099942544/5224354917"); // test code

            AdView adView = new AdView(this);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId("ca-app-pub-4928043878967484~7828914059");
            //adView.setAdUnitId("ca-app-pub-3940256099942544/5224354917"); // test code

            mAdView = findViewById(R.id.ad1);

            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    Log.println(Log.INFO, TAG, "AD LOADED");
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    Log.println(Log.ERROR, TAG, "ERROR WITH AD!!! code = " + errorCode);
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }

                @Override
                public void onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                }

                @Override
                public void onAdClosed() {
                    AdRequest adRequest = new AdRequest.Builder().build();
                    mAdView.loadAd(adRequest);
                }

            });

            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (!connection.isConnectingToInternet()) {
                        mAdView.setVisibility(View.INVISIBLE);
                        Log.println(Log.ERROR, TAG, "Not connection could be established.");
                    } else {
                        mAdView.setVisibility(View.VISIBLE);
                    }

                }
            };
            timer.schedule(task, 1000, 5000);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
            Log.println(Log.ERROR, TAG, "ERROR WITH AD - exception");
        }


        progressBar.setVisibility(View.INVISIBLE);

        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            kenteken = text.getText().toString().toUpperCase();
                            kenteken = kenteken.replace("-", "");
                            if (kenteken.length() > 0) {
                                text.setBackground(getResources().getDrawable(R.drawable.border));
                                text.setText(kenteken);
                                uri = "https://opendata.rdw.nl/resource/m9d7-ebf2.json?kenteken=" + kenteken;
                                progressBar.setVisibility(View.VISIBLE);
                                result.removeAllViews();
                                runner = new Async();
                                runner.execute("1000");
                            } else {
                                text.setBackground(getResources().getDrawable(R.drawable.border_error));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            e.getMessage();
                        }
                        InputMethodManager inputManager = (InputMethodManager) Scan.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    }
                });
        text.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:
                            try {
                                text.setBackground(getResources().getDrawable(R.drawable.border));
                                kenteken = text.getText().toString().toUpperCase();
                                kenteken = kenteken.replace("-", "");
                                if (kenteken.length() > 0) {
                                    text.setText(kenteken);
                                    uri = "https://opendata.rdw.nl/resource/m9d7-ebf2.json?kenteken=" + kenteken;
                                    progressBar.setVisibility(View.VISIBLE);
                                    result.removeAllViews();
                                    runner = new Async();
                                    runner.execute("1000");
                                }
                            } catch (Exception e) {
                                text.setBackground(getResources().getDrawable(R.drawable.border_error));
                                e.printStackTrace();
                                e.getMessage();
                            }
                            InputMethodManager inputManager = (InputMethodManager) Scan.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
    }


    private class Async extends AsyncTask<String, String, String> {

        private String resp;

        @Override
        protected String doInBackground(String... params) {
            try {
                if (!kenteken.isEmpty()) {
                    response = new Request();
                    resp = response.PerformRequest(kenteken);
                    Log.println(Log.INFO, TAG, resp);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resp;
        }

        @Override
        protected void onPostExecute(String Result) {
            if (!Result.equals("No internet connection.")) {
                try {
                    JSONArray array = new JSONArray(Result);

                    JSONObject object = array.getJSONObject(0);
                    Iterator iterator = object.keys();

                    result.setVisibility(View.VISIBLE);

                    LinearLayout lin = new LinearLayout(Scan.this);
                    lin.setOrientation(LinearLayout.VERTICAL);

                    while (iterator.hasNext()) {
                        String key = (String) iterator.next();
                        if (inArray(key)) {
                            String Filtered = key.replace("_", " ");
                            String value = object.getString(key);

                            TextView line = new TextView(Scan.this);
                            TextView line2 = new TextView(Scan.this);

                            if (key.equals("vervaldatum_apk")){
                                try {
                                    if (new SimpleDateFormat("DD/MM/yyyy").parse(value).before(new Date())) {
                                        line2.setBackground(getResources().getDrawable(R.drawable.border_error_item));
                                    }else{
                                        line2.setBackgroundColor(Color.parseColor("#616161"));
                                    }
                                }catch (ParseException PE){
                                    PE.printStackTrace();
                                }
                            }else{
                                line2.setBackgroundColor(Color.parseColor("#616161"));
                            }

                            line.setText(Filtered);
                            line2.setText(value);

                            line.setTextColor(Color.BLACK);
                            line2.setTextColor(Color.BLACK);

                            line.setBackgroundColor(Color.parseColor("#717171"));

                            line.setTextColor(Color.parseColor("#FFFFFF"));
                            line2.setTextColor(Color.parseColor("#FFFFFF"));

                            line.setVisibility(View.VISIBLE);
                            line2.setVisibility(View.VISIBLE);

                            line.setPadding(10, 0, 0, 0);
                            line2.setPadding(10, 0, 0, 0);

                            line.setTextSize(15);
                            line2.setTextSize(15);

                            line.setTypeface(null, Typeface.BOLD);
                            line2.setTypeface(null, Typeface.ITALIC);

                            line.setWidth(100);
                            line2.setWidth(100);

                            try {
                                lin.addView(line);
                                lin.addView(line2);
                            } catch (Exception e) {
                                e.printStackTrace();
                                e.getMessage();
                            }
                        }
                    }
                    result.addView(lin);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                LinearLayout lin = new LinearLayout(Scan.this);
                lin.setOrientation(LinearLayout.VERTICAL);

                TextView line = new TextView(Scan.this);

                line.setText(Result);

                line.setTextColor(Color.RED);

                lin.addView(line);
                result.addView(lin);
            }
            progressBar.setVisibility(View.INVISIBLE);

        }

        public boolean inArray(String attr) {
            if (shownKeys.contains(attr)) {
                Log.println(Log.INFO, TAG, attr + " 1");
                return true;
            }
            Log.println(Log.INFO, TAG, attr + " 0");
            return false;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(String... text) {
        }
    }

    private class Request {

        public String PerformRequest(String kenteken) {
            String result = null;
            String message;
            if (connection.isConnectingToInternet()) {
                try {
                    if (kenteken != "" || kenteken != null) {
                        try {
                            url = new URL(uri);
                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                            try {
                                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                                result = readStream(in);
                                Log.println(Log.INFO, TAG, result);
                            } catch (IOException E) {
                                message = E.getMessage();
                                Log.println(Log.ERROR, TAG, "===> " + message);
                            } finally {
                                urlConnection.disconnect();
                            }
                        } catch (Exception e) {
                            message = e.getMessage();
                            Log.println(Log.ERROR, TAG, " !!===>" + message);
                        }
                    } else {
                        result = "ERROR";
                    }
                } catch (Exception x) {
                    x.getMessage();
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
                Log.e(TAG, "1", e);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "2", e);
                }
            }
            return sb.toString();
        }
    }

    public class ConnectionDetector {

        private Context _context;

        public ConnectionDetector(Context context) {
            this._context = context;
        }

        public boolean isConnectingToInternet() {
            ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null)
                    for (int i = 0; i < info.length; i++)
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }

            }
            return false;
        }
    }
}

