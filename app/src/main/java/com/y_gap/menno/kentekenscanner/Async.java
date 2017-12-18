package com.y_gap.menno.kentekenscanner;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by Menno on 08/12/2017.
 */

class Async extends AsyncTask<String, String, String> {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private static final String TAG = "Async";
    private static final String AUTHOR = "Author => Menno Spijker";
    private static final String NAMEFILE = "data.json";

    private String kenteken, uri;
    private ScrollView result;
    private Context main;
    private ArrayList<String> shownKeys = new ArrayList<String>();
    private ConnectionDetector connection;
    public kentekenHandler Khandler;
    public AdView mAdView;


    Async(Context m, String k, ScrollView r, ArrayList<String> s, String u, ConnectionDetector c, Button re, kentekenHandler kh, AdView mad) {
        try {
            main = m;
            kenteken = k;
            result = r;
            shownKeys = s;
            uri = u;
            connection = c;
            Button recent = re;
            Khandler = kh;
            mAdView = mad;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String resp;

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
    protected void onPostExecute(String Result) {
        if (!Result.equals("No internet connection.")) {
            try {
                JSONArray array = new JSONArray(Result);
                JSONObject object = array.getJSONObject(0);

                try {
                    result.removeAllViews();

                    Iterator iterator = object.keys();

                    result.setVisibility(View.VISIBLE);

                    LinearLayout lin = new LinearLayout(main);
                    lin.setOrientation(LinearLayout.VERTICAL);

                    while (iterator.hasNext()) {
                        String key = (String) iterator.next();
                        //if (inArray(key)) {
                            String Filtered = key.replace("_", " ");
                            String value = object.getString(key);

                            TextView line = new TextView(main);
                            TextView line2 = new TextView(main);

                            if (key.equals("vervaldatum_apk")) {
                                try {
                                    if (new SimpleDateFormat("DD/MM/yyyy").parse(value).before(new Date())) {
                                        line2.setBackground(main.getResources().getDrawable(R.drawable.border_error_item));
                                    } else {
                                        line2.setBackgroundColor(Color.parseColor("#ffffff"));
                                    }
                                } catch (ParseException PE) {
                                    PE.printStackTrace();
                                }
                            } else {
                                line2.setBackgroundColor(Color.parseColor("#ffffff"));
                            }

                            line.setText(Filtered);
                            line2.setText(value);

                            line.setTextColor(Color.BLACK);
                            line2.setTextColor(Color.BLACK);

                            line.setBackgroundColor(Color.parseColor("#eeeeee"));

                            line.setTextColor(Color.parseColor("#222222"));
                            line2.setTextColor(Color.parseColor("#222222"));

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
                    //}
                    Button save = new Button(main);
                    save.setText(R.string.save);

                    save.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Khandler.SaveKenteken(kenteken);
                                    Khandler.openRecent();
                                }
                            });

                    lin.addView(save);
                    result.addView(lin);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (JSONException je) {
                je.printStackTrace();
                je.getMessage();

                LinearLayout lin = new LinearLayout(main);
                lin.setOrientation(LinearLayout.VERTICAL);

                TextView line = new TextView(main);

                line.setText(R.string.no_result);

                line.setTextColor(Color.RED);

                lin.addView(line);
                result.addView(lin);
            }
        } else {
            LinearLayout lin = new LinearLayout(main);
            lin.setOrientation(LinearLayout.VERTICAL);

            TextView line = new TextView(main);

            line.setText(R.string.no_internet_connection);

            line.setTextColor(Color.RED);

            lin.addView(line);
            result.addView(lin);
        }
    }

    private boolean inArray(String attr) {
        return shownKeys.contains(attr);
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(String... text) {}


}