package com.MennoSpijker.kentekenscanner.Factory;

import static com.MennoSpijker.kentekenscanner.view.KentekenHandler.formatLicensePlate;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.MennoSpijker.kentekenscanner.R;
import com.MennoSpijker.kentekenscanner.Util.FileHandling;
import com.MennoSpijker.kentekenscanner.view.KentekenHandler;
import com.MennoSpijker.kentekenscanner.view.MainActivity;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class LicencePlateDataFactory {

    private static final String TAG = "KentekenDataFactory";
    private JSONArray array = new JSONArray();
    private KentekenHandler kentekenHandler;
    private String kenteken;
    private ScrollView resultView;
    private MainActivity context;
    private NotificationFactory notificationFactory;

    public LicencePlateDataFactory() {
        array.put(new JSONObject());
    }

    public void emptyArray() {
        array = new JSONArray();
        array.put(new JSONObject());
    }

    public void addParams(MainActivity context, ScrollView resultView, String kenteken, KentekenHandler Khandler) {
        if (this.context == null || !this.kenteken.equals(kenteken)) {
            this.context = context;
            this.resultView = resultView;
            this.kenteken = kenteken;
            this.kentekenHandler = Khandler;

            this.kentekenHandler.saveRecentKenteken(kenteken);

            this.notificationFactory = new NotificationFactory(context);
        }
    }

    public void fillArray(String kentekenDataFromAPI, ProgressBar progressBar) {
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.setCustomKey("API_RESPONSE", kentekenDataFromAPI);
        crashlytics.setCustomKey("KENTEKEN", this.kenteken);

        progressBar.setProgress(50);
        try {
            JSONObject APIResult = new JSONArray(kentekenDataFromAPI).getJSONObject(0);
            if (APIResult == null) {
                showErrorMessage();
                progressBar.setVisibility(View.GONE);
                return;
            }

            if (kentekenDataFromAPI.length() > 3 && APIResult.length() > 0) {
                progressBar.setProgress(77);
                try {
                    JSONArray array1 = new JSONArray(kentekenDataFromAPI);
                    JSONObject object = array1.getJSONObject(0);

                    Iterator<String> iterator = object.keys();

                    while (iterator.hasNext()) {
                        progressBar.incrementProgressBy(1);

                        String key = (String) iterator.next();
                        String value = object.getString(key);

                        array.getJSONObject(0).put(key, value);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                fillResultView();
            } else {

                if (array.getJSONObject(0).length() == 0) {
                    LinearLayout lin = new LinearLayout(context);
                    lin.setOrientation(LinearLayout.VERTICAL);

                    TextView line = new TextView(context);

                    line.setText(R.string.no_result);

                    line.setTextColor(Color.RED);
                    line.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                    resultView.removeAllViews();
                    resultView.addView(line);
                    progressBar.setVisibility(View.GONE);
                }
            }
            progressBar.setVisibility(View.GONE);
        } catch (JSONException e) {
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);
        }
    }

    public void showErrorMessage() {
        LinearLayout lin = new LinearLayout(context);
        lin.setOrientation(LinearLayout.VERTICAL);

        TextView line = new TextView(context);

        line.setText(R.string.no_result);

        line.setTextColor(Color.RED);
        line.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        resultView.removeAllViews();
        resultView.addView(line);
    }

    Button createSaveButton() {
        final Button save = new Button(context);

        save.setText(R.string.save);

        save.setOnClickListener(v -> {
            kentekenHandler.saveFavoriteKenteken(kenteken);
            kentekenHandler.openSaved();
        });

        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        size.weight = 1;

        save.setLayoutParams(size);

        return save;
    }

    Button createRemoveButton() {
        final Button remove = new Button(context);

        remove.setText(R.string.delete);

        remove.setOnClickListener(v -> {
            try {
                kentekenHandler.deleteFavoriteKenteken(kenteken);
                kentekenHandler.openSaved();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        size.weight = 1;

        remove.setLayoutParams(size);
        return remove;
    }

    Button createNotificationButton() {
        final Button notify = new Button(context);

        notify.setText(R.string.notify);

        notify.setOnClickListener(v -> {
            try {
                String notificationText = "Pas op! De APK van jouw voertuig met het kenteken " + formatLicensePlate(kenteken) + " vervalt over 30 dagen. (Heb je de APK al verlengd? Dan kun je dit bericht negeren!)";

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, formatLicensePlate(kenteken));
                context.firebaseAnalytics.logEvent("notification_created", bundle);

                Log.d(TAG, "createNotificationButton: " + array.getJSONObject(0));
                notificationFactory.planNotification(
                        context.getString(R.string.APK_ALERT),
                        notificationText,
                        kenteken,
                        notificationFactory.calculateNotifcationTime(array.getJSONObject(0).getString("vervaldatum_apk")));

                fillResultView();
                Toast.makeText(context, R.string.notifcationActivated, Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        size.weight = 1;

        notify.setLayoutParams(size);
        return notify;
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    public void fillResultView() {
        try {
            resultView.removeAllViews();
            resultView.setVisibility(View.VISIBLE);

            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            LinearLayout linearLayoutHorizontal = new LinearLayout(context);
            linearLayoutHorizontal.setOrientation(LinearLayout.HORIZONTAL);

            Button button;

            JSONObject kentekens = new FileHandling(context).getSavedKentekens();

            if (kentekens.length() != 0) {

                boolean inArray = false;
                for (int i = 0; i < kentekens.getJSONArray("cars").length(); i++) {
                    if (kentekens.getJSONArray("cars").getString(i).equals(kenteken)) {
                        inArray = true;
                    }
                }

                if (inArray) {
                    button = createRemoveButton();
                } else {
                    button = createSaveButton();
                }
            } else {
                button = createSaveButton();
            }

            linearLayoutHorizontal.addView(button);
            // TODO add notify button
            if (!new FileHandling(context).doesNotificationExist(kenteken)) {
                linearLayoutHorizontal.addView(createNotificationButton());
            }

            linearLayout.addView(linearLayoutHorizontal);

            JSONObject object = array.getJSONObject(0);
            Iterator<String> iterator = object.keys();

            while (iterator.hasNext()) {
                String key = (String) iterator.next();

                if (!key.contains("api")) {
                    String Filtered = key.replace("_", " ");
                    String value = object.getString(key);

                    TextView line = new TextView(context);
                    TextView line2 = new TextView(context);
                    View line3 = new View(context);

                    if (key.equals("kenteken")) {
                        value = formatLicensePlate(value);
                    }

                    if (key.equals("imageURL")) {
                        continue;
                    }

                    if (key.equals("vervaldatum_apk")) {
                        try {
                            Date date = new SimpleDateFormat("dd-MM-yy", Locale.GERMANY).parse(value);

                            if (date != null && date.before(new Date())) {
                                line2.setBackground(context.getResources().getDrawable(R.drawable.border_error_item));
                            } else {
                                line2.setBackgroundColor(Color.parseColor("#ffffff"));
                            }
                        } catch (ParseException PE) {
                            PE.printStackTrace();
                        }
                    }

                    line.setText(Filtered);
                    line2.setText(value);

                    line.setTextColor(Color.BLACK);
                    line2.setTextColor(Color.BLACK);

                    line.setTextColor(Color.parseColor("#222222"));
                    line2.setTextColor(Color.parseColor("#222222"));

                    line.setVisibility(View.VISIBLE);
                    line2.setVisibility(View.VISIBLE);

                    line.setPadding(10, 25, 10, 0);
                    line2.setPadding(10, 10, 10, 25);

                    line.setTextSize(17);
                    line2.setTextSize(15);

                    line.setTypeface(null, Typeface.BOLD);
                    line2.setTypeface(null, Typeface.ITALIC);

                    line.setWidth(100);
                    line2.setWidth(100);

                    line3.setBackgroundColor(Color.parseColor("#aaaaaa"));
                    line3.setMinimumHeight(1);

                    try {
                        linearLayout.addView(line);
                        linearLayout.addView(line2);
                        linearLayout.addView(line3);
                    } catch (Exception e) {
                        e.printStackTrace();
                        e.getMessage();
                    }
                }
            }

            resultView.addView(linearLayout);
        } catch (JSONException je) {
            je.printStackTrace();
            je.getMessage();

            showErrorMessage();
        }
    }
}
