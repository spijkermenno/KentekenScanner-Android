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

import androidx.annotation.NonNull;

import com.MennoSpijker.kentekenscanner.ConnectionDetector;
import com.MennoSpijker.kentekenscanner.FontManager;
import com.MennoSpijker.kentekenscanner.OcrCaptureActivity;
import com.MennoSpijker.kentekenscanner.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    public FirebaseAnalytics mFirebaseAnalytics;

    private static final int RC_OCR_CAPTURE = 9003;
    public Button showHistoryButton, openCameraButton, showFavoritesButton, showAlertsButton;
    public ScrollView resultScrollView;
    public KentekenHandler Khandler;
    private Bundle bundle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);

        Log.d(TAG, "writeToFileOnDate: " + this.getFilesDir());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        final EditText kentekenTextField = findViewById(R.id.kenteken);

        kentekenTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (kentekenTextField.getText().length() == 6) {
                    String formatedLicenceplate = KentekenHandler.formatLicenseplate(kentekenTextField.getText().toString());
                    if (!kentekenTextField.getText().toString().equals(formatedLicenceplate)) {
                        kentekenTextField.setText(formatedLicenceplate);
                        Log.d(TAG, "onTextChanged: VALID KENTEKEN: " +KentekenHandler.kentekenValid(kentekenTextField.getText().toString()));

                        if (KentekenHandler.kentekenValid(kentekenTextField.getText().toString())) {
                            if(KentekenHandler.getSidecodeLicenseplate(kentekenTextField.getText().toString().toUpperCase()) != -1 && KentekenHandler.getSidecodeLicenseplate(kentekenTextField.getText().toString().toUpperCase()) != -2) {
                                Khandler.run(kentekenTextField);
                            }
                        }
                    }
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        showHistoryButton = findViewById(R.id.showHistory);
        openCameraButton = findViewById(R.id.camera);
        showFavoritesButton = findViewById(R.id.showFavorites);
        showAlertsButton = findViewById(R.id.showAlerts);

        resultScrollView = findViewById(R.id.scroll);
        ConnectionDetector connection = new ConnectionDetector(this);

        getAds();

        Khandler = new KentekenHandler(MainActivity.this, connection, showFavoritesButton, resultScrollView, kentekenTextField);

        showHistoryButton.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        showHistoryButton.setTextSize(20);
        openCameraButton.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        openCameraButton.setTextSize(20);
        showFavoritesButton.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        showFavoritesButton.setTextSize(20);
        showAlertsButton.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        showAlertsButton.setTextSize(20);

        showHistoryButton.setOnClickListener(v -> Khandler.openRecent());

        openCameraButton.setOnClickListener(v -> startCameraIntent());

        showFavoritesButton.setOnClickListener(v -> Khandler.openSaved());

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

        showAlertsButton.setOnClickListener(v -> {
            //Khandler.openSaved();
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

            if(KentekenHandler.getSidecodeLicenseplate(kentekenTextField.getText().toString().toUpperCase()) != -1 && KentekenHandler.getSidecodeLicenseplate(kentekenTextField.getText().toString().toUpperCase()) != -2) {
                Log.d(TAG, "onKey: BINGO");
                Khandler.run(kentekenTextField);
                return true;
            }

            return false;
        });
    }

    protected void getAds() {
        MobileAds.initialize(this, initializationStatus -> {
        });

        try {
            AdView adView = new AdView(this);

            adView.setAdUnitId("ca-app-pub-4928043878967484/5146910390");
            adView = this.findViewById(R.id.ad1);

            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    bundle = new Bundle();
                    mFirebaseAnalytics.logEvent("Ad_loaded", bundle);
                }

                @Override
                public void onAdFailedToLoad(LoadAdError adError) {
                    bundle = new Bundle();
                    bundle.putString("Message", adError.getMessage());
                    mFirebaseAnalytics.logEvent("Ad_error", bundle);
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }

                @Override
                public void onAdClicked() {
                    bundle = new Bundle();
                    mFirebaseAnalytics.logEvent("AD_CLICK", bundle);
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                }
            });

            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);

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