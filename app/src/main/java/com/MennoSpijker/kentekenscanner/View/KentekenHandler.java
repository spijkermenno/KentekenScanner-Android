package com.MennoSpijker.kentekenscanner.View;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.MennoSpijker.kentekenscanner.ConnectionDetector;
import com.MennoSpijker.kentekenscanner.Factory.KentekenDataFactory;
import com.MennoSpijker.kentekenscanner.FontManager;
import com.MennoSpijker.kentekenscanner.R;
import com.MennoSpijker.kentekenscanner.Util.FileHandling;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class KentekenHandler {
    private static final String RecentKentekensFile = "recent.json";
    private static final String SavedKentekensFile = "favorites.json";

    private final Button button3;
    private final MainActivity context;

    private final ConnectionDetector connection;
    private final ScrollView result;
    private final TextView kentekenHolder;
    public AdView mAdView;
    private final KentekenDataFactory kentekenDataFactory = new KentekenDataFactory();
    private final Bundle bundle;

    public KentekenHandler(MainActivity c, ConnectionDetector co, Button b, ScrollView r, TextView kHold, AdView mad) {
        this.context = c;
        this.connection = co;
        this.button3 = b;
        this.result = r;
        this.kentekenHolder = kHold;
        this.mAdView = mad;
        bundle = new Bundle();
    }

    public void run(TextView textview) {
        String kenteken = textview.getText().toString().toUpperCase();
        runCamera(kenteken, textview);
    }

    public void runCamera(String kenteken, TextView textview) {
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, formatLicenseplate(kenteken));
        context.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle);

        try {
            kenteken = kenteken.replace("-", "");
            kenteken = kenteken.replace(" ", "");
            kenteken = kenteken.replace("\n", "");
            if (kenteken.length() > 0) {
                kentekenDataFactory.emptyArray();

                textview.setText(formatLicenseplate(kenteken));
                result.removeAllViews();

                String uri = "https://opendata.rdw.nl/resource/m9d7-ebf2.json?kenteken=" + kenteken;
                Async runner = new Async(this.context, kenteken, result, uri, connection, button3, this, kentekenDataFactory);
                runner.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }
        InputMethodManager inputManager = (InputMethodManager) this.context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public JSONObject getSavedKentekens() {
        ArrayList<JSONArray> kentekens = new ArrayList<>();

        String fileContent = new FileHandling().readFile(context, SavedKentekensFile);

        JSONObject mainObject = new JSONObject();
        try {
            mainObject = new JSONObject(fileContent);
        } catch (JSONException e) {
            System.out.println("error empty mainObject");
        }

        //return kentekens;
        return mainObject;
    }

    public JSONObject getRecentKenteken() {
        ArrayList<JSONArray> kentekens = new ArrayList<>();

        String fileContent = new FileHandling().readFile(context, RecentKentekensFile);

        JSONObject mainObject = new JSONObject();
        try {
            mainObject = new JSONObject(fileContent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //return kentekens;
        return mainObject;
    }

    public void saveRecentKenteken(String kenteken) {
        JSONObject otherKentekens = getRecentKenteken();

        SimpleDateFormat wantedFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        String date = wantedFormat.format(new Date());

        new FileHandling().writeToFileOnDate(context, RecentKentekensFile, kenteken, date, otherKentekens);

    }

    public void saveFavoriteKenteken(String kenteken) {
        JSONObject otherKentekens = getSavedKentekens();

        context.mFirebaseAnalytics.setUserProperty("kenteken", kenteken);

        new FileHandling().writeToFile(context, SavedKentekensFile, kenteken, otherKentekens);

    }

    public void openRecent() {
        kentekenHolder.setText("");
        kentekenHolder.clearFocus();

        InputMethodManager inputManager = (InputMethodManager) this.context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

        result.removeAllViews();

        final float scale = context.getResources().getDisplayMetrics().density;
        int width = (int) (283 * scale + 0.5f);
        int height = (int) (64 * scale + 0.5f);

        try {
            final JSONObject recents = getRecentKenteken();

            LinearLayout lin = new LinearLayout(context);
            lin.setOrientation(LinearLayout.VERTICAL);

            Iterator iterator = recents.keys();

            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                JSONArray values = recents.getJSONArray(key);

                TextView dateView = new TextView(context);
                dateView.setText(key);
                dateView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                lin.addView(dateView);

                for (int i = 0; i < values.length(); i++) {
                    String recent = values.getString(i);

                    recent = recent.replace("/", "");

                    Button line = new Button(context);
                    line.setText(KentekenHandler.formatLicenseplate(recent));
                    final String finalRecent = recent;

                    line.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                    line.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    runCamera(finalRecent, kentekenHolder);
                                }
                            });

                    line.setBackground(context.getDrawable(R.drawable.kenteken_v2));

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            width,
                            height
                    );
                    params.setMargins(0, 10, 0, 10);
                    params.gravity = 17;
                    line.setLayoutParams(params);

                    line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
                    line.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

                    int left = (int) (20 * scale + 0.5f);
                    int right = (int) (10 * scale + 0.5f);
                    int top = (int) (0 * scale + 0.5f);
                    int bottom = (int) (0 * scale + 0.5f);

                    line.setPadding(left, top, right, bottom);

                    lin.addView(line);
                }
            }

            if (recents.length() > 0) {
                Button clear = new Button(context);

                clear.setTypeface(FontManager.getTypeface(context, FontManager.FONTAWESOME));
                clear.setText(R.string.fa_icon_trash);
                clear.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new FileHandling().emptyFile(context, RecentKentekensFile);
                                openRecent();
                            }
                        });

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        width,
                        height
                );

                params.gravity = 17;
                clear.setLayoutParams(params);

                lin.addView(clear);
            }

            result.addView(lin);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openSaved() {
        kentekenHolder.setText("");
        kentekenHolder.clearFocus();

        InputMethodManager inputManager = (InputMethodManager) this.context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

        result.removeAllViews();

        final float scale = context.getResources().getDisplayMetrics().density;
        int width = (int) (283 * scale + 0.5f);
        int height = (int) (64 * scale + 0.5f);

        try {
            final JSONObject recents = getSavedKentekens();

            LinearLayout lin = new LinearLayout(context);
            lin.setOrientation(LinearLayout.VERTICAL);

            Iterator iterator = recents.keys();

            TextView textView = new TextView(context);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setText(R.string.eigen_auto);

            System.out.println(recents.names());
            if (recents.getJSONArray(recents.names().getString(0)).length() > 1) {
                textView.setText(R.string.eigen_autos);
            }

            lin.addView(textView);

            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                JSONArray values = recents.getJSONArray(key);


                for (int i = 0; i < values.length(); i++) {
                    String recent = values.getString(i);

                    recent = recent.replace("/", "");

                    Button line = new Button(context);
                    line.setText(KentekenHandler.formatLicenseplate(recent));
                    final String finalRecent = recent;

                    line.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                    line.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    runCamera(finalRecent, kentekenHolder);
                                }
                            });

                    line.setBackground(context.getDrawable(R.drawable.kenteken_v2));

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            width,
                            height
                    );
                    params.setMargins(0, 10, 0, 10);
                    params.gravity = 17;
                    line.setLayoutParams(params);

                    line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
                    line.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

                    int left = (int) (20 * scale + 0.5f);
                    int right = (int) (10 * scale + 0.5f);
                    int top = (int) (0 * scale + 0.5f);
                    int bottom = (int) (0 * scale + 0.5f);

                    line.setPadding(left, top, right, bottom);

                    lin.addView(line);
                }
            }

            if (recents.length() > 0) {
                Button clear = new Button(context);

                clear.setTypeface(FontManager.getTypeface(context, FontManager.FONTAWESOME));
                clear.setText(R.string.fa_icon_trash);
                clear.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new FileHandling().emptyFile(context, SavedKentekensFile);
                                openRecent();
                            }
                        });

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        width,
                        height
                );

                params.gravity = 17;
                clear.setLayoutParams(params);

                lin.addView(clear);
            }

            result.addView(lin);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String formatLicenseplate(String alicenseplate) {

        try {
            int sidecode = getSidecodeLicenseplate(alicenseplate);

            String licenseplate = alicenseplate.replace("-", "").toUpperCase();

            if (sidecode == -2) {
                return alicenseplate;
            }

            if (sidecode <= 6) {
                System.out.println("sidecode 6");
                return licenseplate.substring(0, 2) + '-' + licenseplate.substring(2, 4) + '-' + licenseplate.substring(4, 6);
            }
            if (sidecode == 7 || sidecode == 9) {
                System.out.println("sidecode 7");
                String s = licenseplate.substring(0, 2) + '-' + licenseplate.substring(2, 5) + '-' + licenseplate.substring(5, 6);
                return s;
            }
            if (sidecode == 8 || sidecode == 10) {
                System.out.println("sidecode 8");
                return licenseplate.substring(0, 1) + '-' + licenseplate.substring(1, 4) + '-' + licenseplate.substring(4, 6);
            }
            if (sidecode == 11 || sidecode == 14) {
                System.out.println("sidecode 11");
                return licenseplate.substring(0, 3) + '-' + licenseplate.substring(3, 5) + '-' + licenseplate.substring(5, 6);
            }
            if (sidecode == 12 || sidecode == 13) {
                System.out.println("sidecode 12");
                return licenseplate.substring(0, 1) + '-' + licenseplate.substring(1, 3) + '-' + licenseplate.substring(3, 6);
            }

            return alicenseplate;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return alicenseplate;
    }

    public static int getSidecodeLicenseplate(String licenseplate) {

        String[] patterns = new String[14];

        licenseplate = licenseplate.replace("-", "").toUpperCase();

        patterns[0] = "^[a-zA-Z]{2}[0-9]{2}[0-9]{2}$"; // 1 XX-99-99
        patterns[1] = "^[0-9]{2}[0-9]{2}[a-zA-Z]{2}$"; // 2 99-99-XX
        patterns[2] = "^[0-9]{2}[a-zA-Z]{2}[0-9]{2}$"; // 3 99-XX-99
        patterns[3] = "^[a-zA-Z]{2}[0-9]{2}[a-zA-Z]{2}$"; // 4 XX-99-XX
        patterns[4] = "^[a-zA-Z]{2}[a-zA-Z]{2}[0-9]{2}$"; // 5 XX-XX-99
        patterns[5] = "^[0-9]{2}[a-zA-Z]{2}[a-zA-Z]{2}$"; // 6 99-XX-XX
        patterns[6] = "^[0-9]{2}[a-zA-Z]{3}[0-9]{1}$"; // 7 99-XXX-9
        patterns[7] = "^[0-9]{1}[a-zA-Z]{3}[0-9]{2}$"; // 8 9-XXX-99
        patterns[8] = "^[a-zA-Z]{2}[0-9]{3}[a-zA-Z]{1}$"; // 9 XX-999-X
        patterns[9] = "^[a-zA-Z]{1}[0-9]{3}[a-zA-Z]{2}$"; // 10 X-999-XX
        patterns[10] = "^[a-zA-Z]{3}[0-9]{2}[a-zA-Z]{1}$"; // 11 XXX-99-X
        patterns[11] = "^[a-zA-Z]{1}[0-9]{2}[a-zA-Z]{3}$"; // 12 X-99-XXX
        patterns[12] = "^[0-9]{1}[a-zA-Z]{2}[0-9]{3}$"; // 13 9-XX-999
        patterns[13] = "^[0-9]{3}[a-zA-Z]{2}[0-9]{1}$"; // 14 999-XX-9

        //except licenseplates for diplomats
        String diplomat = "^CD[ABFJNST][0-9]{1,3}$"; //for example: CDB1 of CDJ45

        for (int i = 0; i < patterns.length; i++) {
            if (licenseplate.matches(patterns[i])) {
                return i + 1;
            }
        }
        if (licenseplate.matches(diplomat)) {
            return -1;
        }

        return -2;
    }

    public static boolean kentekenValid(String s) {
        return KentekenHandler.getSidecodeLicenseplate(s) >= -1;
    }
}
