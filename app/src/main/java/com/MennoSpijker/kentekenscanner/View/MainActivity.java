package com.MennoSpijker.kentekenscanner.View;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

import com.MennoSpijker.kentekenscanner.ConnectionDetector;
import com.MennoSpijker.kentekenscanner.Factory.NotificationFactory;
import com.MennoSpijker.kentekenscanner.FontManager;
import com.MennoSpijker.kentekenscanner.OcrCaptureActivity;
import com.MennoSpijker.kentekenscanner.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final int RC_OCR_CAPTURE = 9003;

    private Bundle bundle;

    public FirebaseAnalytics firebaseAnalytics;
    public Button showHistoryButton, openCameraButton, showFavoritesButton, showAlertsButton;
    public ArrayList<Button> buttons = new ArrayList<>();
    public ScrollView resultScrollView;
    public KentekenHandler Khandler;
    private NotificationFactory notificationFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        final EditText kentekenTextField = findViewById(R.id.kenteken);
        Log.d(TAG, "onCreate: " + kentekenTextField);

        showHistoryButton = findViewById(R.id.showHistory);
        openCameraButton = findViewById(R.id.camera);
        showFavoritesButton = findViewById(R.id.showFavorites);
        showAlertsButton = findViewById(R.id.showAlerts);

        buttons.add(showHistoryButton);
        buttons.add(openCameraButton);
        buttons.add(showFavoritesButton);
        buttons.add(showAlertsButton);

        resultScrollView = findViewById(R.id.scroll);
        ConnectionDetector connectionDetector = new ConnectionDetector(this);

        Log.d(TAG, "onCreate: " + kentekenTextField);

        Khandler = new KentekenHandler(MainActivity.this, connectionDetector, showFavoritesButton, resultScrollView, kentekenTextField);

        buttons.forEach((button) -> {
            button.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
            button.setTextSize(20);
        });

        showHistoryButton.setOnClickListener(v -> Khandler.openRecent());

        openCameraButton.setOnClickListener(v -> startCameraIntent());

        showFavoritesButton.setOnClickListener(v -> Khandler.openSaved());

        showAlertsButton.setOnClickListener(v -> {
            // TODO: create alerts
            //Khandler.openSaved();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Must be run on main UI thread...
        getAds();

        // Run the setup ASYNC for faster first render.
        new Thread(() -> {
            final EditText kentekenTextField = findViewById(R.id.kenteken);

            kentekenTextField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    Log.d(TAG, "beforeTextChanged: " + charSequence.toString());
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String kenteken = kentekenTextField.getText().toString();

                    // check if kenteken is 6 characters long
                    if (kenteken.length() == 6) {
                        String formatedKenteken = KentekenHandler.formatLicenseplate(kenteken);
                        if (!kenteken.equals(formatedKenteken)) {
                            // Set formatted text in kentekenField
                            kentekenTextField.setText(formatedKenteken);
                            // check if kenteken is valid
                            if (KentekenHandler.kentekenValid(kentekenTextField.getText().toString())) {
                                // run API call
                                Khandler.run(kentekenTextField);
                            }
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    Log.d(TAG, "afterTextChanged: " + editable.toString());
                }
            });

            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log
                        Log.d("FCM Token", token);
                    });

            kentekenTextField.setOnKeyListener((v, keyCode, event) -> {

                Log.d(TAG, "onKey: " + keyCode);
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:
                            Khandler.run(kentekenTextField);
                            return true;

                        case KeyEvent.KEYCODE_DEL:
                            Log.d(TAG, "onKey: KEY EVENT");
                            String text = kentekenTextField.getText().toString();
                            text = text.replace("-", "");

                            String newText = text;

                            if (text.length() > 0) {
                                newText = text.substring(0, text.length() - 1);
                            }

                            kentekenTextField.setText(newText);
                            kentekenTextField.setSelection(kentekenTextField.getText().length());
                            return true;
                        default:
                            break;
                    }
                }

                if (KentekenHandler.getSidecodeLicenseplate(kentekenTextField.getText().toString().toUpperCase()) != -1 && KentekenHandler.getSidecodeLicenseplate(kentekenTextField.getText().toString().toUpperCase()) != -2) {
                    Log.d(TAG, "onKey: BINGO");
                    Khandler.run(kentekenTextField);
                    return true;
                }

                return false;
            });

        }).start();
    }

    protected void getAds() {
        MobileAds.initialize(this, initializationStatus -> {
        });

        try {
            AdView advertisementView = new AdView(this);

            advertisementView.setAdUnitId("ca-app-pub-4928043878967484/5146910390");
            advertisementView = this.findViewById(R.id.ad1);

            advertisementView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    bundle = new Bundle();
                    firebaseAnalytics.logEvent("Ad_loaded", bundle);
                }

                @Override
                public void onAdFailedToLoad(LoadAdError adError) {
                    bundle = new Bundle();
                    bundle.putString("Message", adError.getMessage());
                    firebaseAnalytics.logEvent("Ad_error", bundle);
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }

                @Override
                public void onAdClicked() {
                    bundle = new Bundle();
                    firebaseAnalytics.logEvent("AD_CLICK", bundle);
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                }
            });

            AdRequest adRequest = new AdRequest.Builder().build();
            advertisementView.loadAd(adRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCameraIntent() {
        try {
            Intent intent = new Intent(this, OcrCaptureActivity.class);
            intent.putExtra(OcrCaptureActivity.AutoFocus, true);
            intent.putExtra(OcrCaptureActivity.UseFlash, false);

            startActivityForResult(intent, RC_OCR_CAPTURE);
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_OCR_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    String text = data.getStringExtra(OcrCaptureActivity.TextBlockObject);
                    final EditText textfield = findViewById(R.id.kenteken);
                    Khandler.runCamera(text, textfield);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}