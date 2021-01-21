package com.MennoSpijker.kentekenscanner.Factory;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.MennoSpijker.kentekenscanner.ConnectionDetector;
import com.MennoSpijker.kentekenscanner.R;
import com.MennoSpijker.kentekenscanner.View.Async;
import com.MennoSpijker.kentekenscanner.View.KentekenHandler;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class KentekenDataFactory {

    private JSONArray array = new JSONArray();
    private KentekenHandler kentekenHandler;
    private String kenteken;
    private ScrollView resultView;
    private Context context;
    private ConnectionDetector connection;
    private Button recentButton;
    private Bundle bundle;

    public KentekenDataFactory() {
        array.put(new JSONObject());
        bundle = new Bundle();
    }

    public void emptyArray() {
        array = new JSONArray();
        array.put(new JSONObject());
    }

    public void addParams(Context main, ScrollView resultView, String kenteken, KentekenHandler Khandler, ConnectionDetector connection, Button recentButton) {
        if (context == null || !this.kenteken.equals(kenteken)) {
            this.context = main;
            this.resultView = resultView;
            this.kenteken = kenteken;
            this.kentekenHandler = Khandler;
            this.connection = connection;
            this.recentButton = recentButton;

            this.kentekenHandler.saveRecentKenteken(kenteken);
        }
    }

    public void fillArray(String results) {
        if (results.length() > 3) {
            try {
                JSONArray array1 = new JSONArray(results);
                JSONObject object = array1.getJSONObject(0);

                Iterator iterator = object.keys();

                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    String value = object.getString(key);

                    array.getJSONObject(0).put(key, value);

                    if (key.contains("api")) {
                        String uri2 = value + "?kenteken=" + kenteken;
                        Async runner = new Async(context, kenteken, resultView, uri2, connection, recentButton, kentekenHandler, this);
                        runner.execute();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            fillResultView();
        } else {
            try {
                if (array.getJSONObject(0).length() == 0) {
                    LinearLayout lin = new LinearLayout(context);
                    lin.setOrientation(LinearLayout.VERTICAL);

                    TextView line = new TextView(context);

                    line.setText(R.string.no_result);

                    line.setTextColor(Color.RED);
                    line.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                    resultView.removeAllViews();
                    resultView.addView(line);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public JSONArray getArray() {
        return array;
    }


    public void fillResultView() {
        try {
            resultView.removeAllViews();
            resultView.setVisibility(View.VISIBLE);

            LinearLayout lin = new LinearLayout(context);
            lin.setOrientation(LinearLayout.VERTICAL);

            Button save = new Button(context);
            save.setText(R.string.save);

            save.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            kentekenHandler.saveRecentKenteken(kenteken);
                        }
                    });

            lin.addView(save);

            JSONObject object = array.getJSONObject(0);
            Iterator iterator = object.keys();

            while (iterator.hasNext()) {
                String key = (String) iterator.next();

                if (!key.contains("api")) {
                    String Filtered = key.replace("_", " ");
                    String value = object.getString(key);

                    TextView line = new TextView(context);
                    TextView line2 = new TextView(context);

                    if (key.equals("kenteken")) {
                        value = KentekenHandler.formatLicenseplate(value);
                    }

                    if (key.equals("datum_tenaamstelling")) {
                        value = value.substring(6, 8) + "-" + value.substring(4, 6) + "-" + value.substring(0, 4);
                    }

                    if (key.equals("datum_eerste_toelating")) {
                        value = value.substring(6, 8) + "-" + value.substring(4, 6) + "-" + value.substring(0, 4);
                    }

                    if (key.equals("datum_eerste_afgifte_nederland")) {
                        value = value.substring(6, 8) + "-" + value.substring(4, 6) + "-" + value.substring(0, 4);
                    }

                    if (key.equals("vervaldatum_apk")) {
                        try {
                            String date = value.substring(6, 8) + "-" + value.substring(4, 6) + "-" + value.substring(0, 4);
                            if (new SimpleDateFormat("yyyyMMdd").parse(value).before(new Date())) {
                                line2.setBackground(context.getResources().getDrawable(R.drawable.border_error_item));
                            } else {
                                line2.setBackgroundColor(Color.parseColor("#ffffff"));
                            }

                            value = date;
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

                    line.setPadding(10, 10, 10, 0);
                    line2.setPadding(10, 10, 10, 0);

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

            resultView.addView(lin);
        } catch (JSONException je) {
            je.printStackTrace();
            je.getMessage();

            LinearLayout lin = new LinearLayout(context);
            lin.setOrientation(LinearLayout.VERTICAL);

            TextView line = new TextView(context);

            line.setText(R.string.no_result);

            line.setTextColor(Color.RED);

            lin.addView(line);
            resultView.addView(lin);
        }
    }
}
