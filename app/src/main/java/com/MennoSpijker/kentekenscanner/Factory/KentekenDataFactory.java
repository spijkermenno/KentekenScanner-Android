package com.MennoSpijker.kentekenscanner.Factory;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.MennoSpijker.kentekenscanner.ConnectionDetector;
import com.MennoSpijker.kentekenscanner.Font.FontManager;
import com.MennoSpijker.kentekenscanner.Font.IconType;
import com.MennoSpijker.kentekenscanner.R;
import com.MennoSpijker.kentekenscanner.View.Async;
import com.MennoSpijker.kentekenscanner.View.MainActivity;
import com.MennoSpijker.kentekenscanner.View.SearchHandler;
import com.google.android.gms.ads.AdSize;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class KentekenDataFactory {

    private JSONArray array = new JSONArray();
    private SearchHandler searchHandler;
    private String kenteken;
    private ScrollView resultView;
    private MainActivity context;
    private ConnectionDetector connection;
    private Button recentButton;
    private Bundle bundle;
    private Date apkDate = null;
    private boolean newApiCall = false;

    public KentekenDataFactory() {
        array.put(new JSONObject());
        bundle = new Bundle();
    }

    public void emptyArray() {
        array = new JSONArray();
        array.put(new JSONObject());
    }

    public void addParams(MainActivity main, ScrollView resultView, String kenteken, SearchHandler Khandler, ConnectionDetector connection, Button recentButton) {
        if (context == null || !this.kenteken.equals(kenteken)) {
            this.context = main;
            this.resultView = resultView;
            this.kenteken = kenteken;
            this.searchHandler = Khandler;
            this.connection = connection;
            this.recentButton = recentButton;

            this.searchHandler.saveRecentKenteken(kenteken);
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
                        newApiCall = true;
                        String uri2 = value + "?kenteken=" + kenteken;
                        Async runner = new Async(context, kenteken, resultView, uri2, connection, recentButton, searchHandler, this);
                        runner.execute();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            fillResultView();
            newApiCall = false;
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

    public TextView defaultTextview(int loopnumber, String value, boolean title) {
        TextView t = new TextView(context);

        t.setTextColor(Color.BLACK);

        t.setBackgroundColor(Color.parseColor("#eaeaea"));

        if (loopnumber % 2 == 0) {
            t.setBackgroundColor(Color.parseColor("#dddddd"));
        }

        t.setTextColor(Color.parseColor("#222222"));

        t.setVisibility(View.VISIBLE);

        t.setTextSize(15);
        t.setTextSize(15);

        if (title) {
            t.setTypeface(null, Typeface.BOLD);
            t.setPadding(10, 10, 10, 0);
        } else {
            t.setTypeface(null, Typeface.ITALIC);
            t.setPadding(10, 10, 10, 10);
        }

        t.setWidth(100);

        t.setText(value);

        return t;
    }


    public void fillResultView() {
        if (!newApiCall) {
            try {
                resultView.removeAllViews();
                resultView.setVisibility(View.VISIBLE);

                LinearLayout lin = new LinearLayout(context);
                lin.setOrientation(LinearLayout.VERTICAL);

                Button save = new Button(context);
                save.setTypeface(FontManager.getTypeface(context, FontManager.setIconType(IconType.REGULAR)));
                save.setText(R.string.fa_icon_star);

                // TODO see if kenteken is favorite.
                if (searchHandler.isFavoriteKenteken(kenteken)) {
                    save.setTypeface(FontManager.getTypeface(context, FontManager.setIconType(IconType.SOLID)));
                }

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

                params.gravity = RelativeLayout.ALIGN_PARENT_END;

                save.setLayoutParams(params);

                save.setBackgroundResource(R.drawable.transparent);

                lin.addView(save);

                JSONObject object = array.getJSONObject(0);
                Iterator iterator = object.keys();

                int loopNumber = 0;
                while (iterator.hasNext()) {
                    loopNumber++;
                    String key = (String) iterator.next();


                    if (!key.contains("api")) {
                        String title = key.replace("_", " ");
                        String value = object.getString(key);

                        if (key.equals("kenteken")) {
                            value = SearchHandler.formatLicenseplate(value);
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

                        if (key.equals("nettomaximumvermogen")) {
                            String titleTemp = "Vermogen PK";
                            String contentTemp = "" + (int) (Float.parseFloat(value) * 1.362);

                            TextView titleViewTemp = defaultTextview(loopNumber, titleTemp, true);
                            TextView contentViewTemp = defaultTextview(loopNumber, contentTemp, false);

                            lin.addView(titleViewTemp);
                            lin.addView(contentViewTemp);
                            loopNumber++;
                        }

                        TextView titleView = defaultTextview(loopNumber, title, true);
                        TextView contentView = defaultTextview(loopNumber, value, false);

                        if (key.equals("nettomaximumvermogen")) {
                            // 0.734
                            titleView.setText("Vermogen KW");
                            contentView.setText("" + ((int) Float.parseFloat(value)));
                        }

                        if (key.equals("vervaldatum_apk")) {
                            try {
                                String date = value.substring(6, 8) + "-" + value.substring(4, 6) + "-" + value.substring(0, 4);
                                apkDate = new SimpleDateFormat("yyyyMMdd").parse(value);
                                if (apkDate.before(new Date())) {
                                    contentView.setBackground(context.getResources().getDrawable(R.drawable.border_error_item));
                                }
                                value = date;
                            } catch (ParseException PE) {
                                PE.printStackTrace();
                            }
                        }

                        if (loopNumber % 20 == 0) {
                            lin.addView(context.factory.createBanner(AdSize.SMART_BANNER));
                        }

                        lin.addView(titleView);
                        lin.addView(contentView);

                    }
                }

                lin.addView(context.factory.createBanner(AdSize.SMART_BANNER));

                if (apkDate != null) {
                    final Date finalApkDate = apkDate;
                    save.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    searchHandler.saveFavoriteKenteken(kenteken, finalApkDate);
                                }
                            });
                } else {
                    save.setVisibility(View.INVISIBLE);
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
        } else {
            // TODO show loading icon
            System.out.println("loading...");
        }
    }
}
