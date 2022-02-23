package com.MennoSpijker.kentekenscanner.View;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.MennoSpijker.kentekenscanner.ConnectionDetector;
import com.MennoSpijker.kentekenscanner.Factory.KentekenDataFactory;
import com.MennoSpijker.kentekenscanner.R;
import com.MennoSpijker.kentekenscanner.Util.FileHandling;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class KentekenHandler {
    private static final String RecentKentekensFile = "recent.json";
    private static final String SavedKentekensFile = "favorites.json";
    private static final String TAG = "KentekenHandler";

    private final MainActivity context;

    private final ConnectionDetector connection;
    private final ScrollView result;
    private final TextView kentekenHolder;
    private final KentekenDataFactory kentekenDataFactory = new KentekenDataFactory();
    private final Bundle bundle;

    private String previousSearchedKenteken = "";

    public KentekenHandler(MainActivity c, ConnectionDetector co, ScrollView r, TextView kHold) {
        this.context = c;
        this.connection = co;
        this.result = r;
        this.kentekenHolder = kHold;
        bundle = new Bundle();

        Log.d(TAG, "KentekenHandler: " + this.kentekenHolder);
        Log.d(TAG, "KentekenHandler: " + kHold);
    }

    public void run(TextView textview) {
        String kenteken = textview.getText().toString().toUpperCase();
        runCamera(kenteken, textview);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void runCamera(String kenteken, TextView textview) {
        ProgressBar progressBar = context.findViewById(R.id.progressBar);
        progressBar.setProgress(10);
        progressBar.setVisibility(View.VISIBLE);

        Log.d(TAG, "runCamera: " + progressBar);
        
        if (this.getPreviousSearchedKenteken().equals(kenteken)) {
            Log.d(TAG, "runCamera: Kenteken run twice, returning...");
            return;
        }

        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, formatLicenseplate(kenteken));
        context.firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle);

        this.setPreviousSearchedKenteken(kenteken);

        progressBar.setProgress(15);

        try {
            kenteken = kenteken.replace("-", "");
            kenteken = kenteken.replace(" ", "");
            kenteken = kenteken.replace("\n", "");
            if (kenteken.length() > 0) {
                kentekenDataFactory.emptyArray();

                textview.setText(formatLicenseplate(kenteken));
                result.removeAllViews();

                String uri = "https://kenteken-scanner.nl/api/kenteken/" + kenteken;
                Async runner = new Async(this.context, kenteken, result, uri, connection, this, kentekenDataFactory, progressBar);
                runner.execute();
            } else {
                progressBar.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
            progressBar.setVisibility(View.GONE);
        }
        InputMethodManager inputManager = (InputMethodManager) this.context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        this.setPreviousSearchedKenteken("");
    }

    public void saveRecentKenteken(String kenteken) {
        JSONObject otherKentekens = new FileHandling(context).getRecentKenteken();

        SimpleDateFormat wantedFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        String date = wantedFormat.format(new Date());

        new FileHandling(context).writeToFileOnDate(RecentKentekensFile, kenteken, date, otherKentekens);

    }

    public void saveFavoriteKenteken(String kenteken) {
        JSONObject otherKentekens = new FileHandling(context).getSavedKentekens();

        context.firebaseAnalytics.setUserProperty("kenteken", kenteken);

        new FileHandling(context).writeToFile(SavedKentekensFile, kenteken, otherKentekens);

    }

    public void deleteFavoriteKenteken(String kenteken) throws JSONException {
        JSONObject otherKentekens = new FileHandling(context).getSavedKentekens();

        context.firebaseAnalytics.setUserProperty("kenteken", kenteken);

        JSONObject temp = new JSONObject();
        temp.put("cars", new JSONArray());

        for (int i = 0; i < otherKentekens.getJSONArray("cars").length(); i++) {
            if (!otherKentekens.getJSONArray("cars").getString(i).equals(kenteken)) {
                temp.getJSONArray("cars").put(otherKentekens.getJSONArray("cars").getString(i));
            }
        }

        Log.d(TAG, "deleteFavoriteKenteken: " + temp);

        new FileHandling(context).writeToFile(SavedKentekensFile, temp);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void openRecent() {
        kentekenHolder.setText("");
        kentekenHolder.clearFocus();

        InputMethodManager inputManager = (InputMethodManager) this.context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

        result.removeAllViews();

        final float scale = context.getResources().getDisplayMetrics().density;
        int width = (int) (283 * scale + 0.5f);
        int height = (int) (75 * scale + 0.5f);

        try {
            final JSONObject recents = new FileHandling(context).getRecentKenteken();

            LinearLayout lin = new LinearLayout(context);
            lin.setOrientation(LinearLayout.VERTICAL);

            Iterator<String> iterator = recents.keys();

            while (iterator.hasNext()) {
                String key = iterator.next();
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
                            v -> runCamera(finalRecent, kentekenHolder));

                    line.setBackground(context.getDrawable(R.drawable.kentekenplaat3));

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            width,
                            height
                    );
                    params.setMargins(0, 10, 0, 10);
                    params.gravity = 17;
                    line.setLayoutParams(params);

                    line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
                    line.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

                    int left = (int) (45 * scale + 0.5f);
                    int right = (int) (10 * scale + 0.5f);
                    int top = (int) (0 * scale + 0.5f);
                    int bottom = (int) (0 * scale + 0.5f);

                    line.setPadding(left, top, right, bottom);

                    lin.addView(line);
                }
            }

            result.addView(lin);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void openSaved() {
        kentekenHolder.setText("");
        kentekenHolder.clearFocus();

        InputMethodManager inputManager = (InputMethodManager) this.context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

        result.removeAllViews();

        final float scale = context.getResources().getDisplayMetrics().density;
        int width = (int) (283 * scale + 0.5f);
        int height = (int) (75 * scale + 0.5f);

        try {
            final JSONObject recents = new FileHandling(context).getSavedKentekens();

            LinearLayout lin = new LinearLayout(context);
            lin.setOrientation(LinearLayout.VERTICAL);

            Iterator<String> iterator = recents.keys();

            TextView textView = new TextView(context);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setText(R.string.eigen_auto);

            System.out.println(recents.names());

            lin.addView(textView);

            while (iterator.hasNext()) {
                String key = iterator.next();
                JSONArray values = recents.getJSONArray(key);


                for (int i = 0; i < values.length(); i++) {
                    String recent = values.getString(i);

                    recent = recent.replace("/", "");

                    Button line = new Button(context);
                    line.setText(KentekenHandler.formatLicenseplate(recent));
                    final String finalRecent = recent;

                    line.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                    line.setOnClickListener(
                            v -> runCamera(finalRecent, kentekenHolder));

                    line.setBackground(context.getDrawable(R.drawable.kentekenplaat3));

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            width,
                            height
                    );
                    params.setMargins(0, 10, 0, 10);
                    params.gravity = 17;
                    line.setLayoutParams(params);

                    line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
                    line.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

                    int left = (int) (45 * scale + 0.5f);
                    int right = (int) (10 * scale + 0.5f);
                    int top = (int) (0 * scale + 0.5f);
                    int bottom = (int) (0 * scale + 0.5f);

                    line.setPadding(left, top, right, bottom);

                    lin.addView(line);
                }
            }

            result.addView(lin);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void openNotifications() {
        kentekenHolder.setText("");
        kentekenHolder.clearFocus();

        // Hide keyboard
        InputMethodManager inputManager = (InputMethodManager) this.context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

        result.removeAllViews();

        final float scale = context.getResources().getDisplayMetrics().density;
        int width = (int) (283 * scale + 0.5f);
        int height = (int) (75 * scale + 0.5f);

        try {
            final JSONObject pendingNotifications = new FileHandling(context).getPendingNotifications();

            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            Iterator<String> iterator = pendingNotifications.keys();
            
            while (iterator.hasNext()) {
                String key = iterator.next();
                JSONArray values = pendingNotifications.getJSONArray(key);

                for (int i = 0; i < values.length(); i++) {
                    JSONObject notification = values.getJSONObject(i);
                    String kenteken = notification.getString("kenteken");
                    Log.d(TAG, "openNotifications: " + kenteken);

                    TextView dateView = new TextView(context);
                    dateView.setText(notification.getString("notificationDate"));
                    dateView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                    linearLayout.addView(dateView);

                    Button button = new Button(context);
                    button.setText(formatLicenseplate(kenteken));

                    button.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                    button.setOnClickListener(v -> runCamera(kenteken, kentekenHolder));

                    button.setBackground(context.getDrawable(R.drawable.kentekenplaat3));

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            width,
                            height
                    );
                    params.setMargins(0, 10, 0, 10);
                    params.gravity = 17;
                    button.setLayoutParams(params);

                    button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
                    button.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

                    int left = (int) (45 * scale + 0.5f);
                    int right = (int) (10 * scale + 0.5f);
                    int top = (int) (0 * scale + 0.5f);
                    int bottom = (int) (0 * scale + 0.5f);

                    button.setPadding(left, top, right, bottom);

                    linearLayout.addView(button);
                }
            }

            result.addView(linearLayout);
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
                return licenseplate.substring(0, 2) + '-' + licenseplate.substring(2, 5) + '-' + licenseplate.charAt(5);
            }
            if (sidecode == 8 || sidecode == 10) {
                System.out.println("sidecode 8");
                return licenseplate.substring(0, 1) + '-' + licenseplate.substring(1, 4) + '-' + licenseplate.substring(4, 6);
            }
            if (sidecode == 11 || sidecode == 14) {
                System.out.println("sidecode 11");
                return licenseplate.substring(0, 3) + '-' + licenseplate.substring(3, 5) + '-' + licenseplate.charAt(5);
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

    public String getPreviousSearchedKenteken() {
        return previousSearchedKenteken;
    }

    public void setPreviousSearchedKenteken(String previousSearchedKenteken) {
        this.previousSearchedKenteken = previousSearchedKenteken;
    }
}
