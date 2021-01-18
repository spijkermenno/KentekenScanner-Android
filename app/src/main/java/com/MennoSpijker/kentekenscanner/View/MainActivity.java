package com.MennoSpijker.kentekenscanner.View;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.MennoSpijker.kentekenscanner.ConnectionDetector;
import com.MennoSpijker.kentekenscanner.FontManager;
import com.MennoSpijker.kentekenscanner.OcrCaptureActivity;
import com.MennoSpijker.kentekenscanner.R;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends Activity {
    public FirebaseAnalytics mFirebaseAnalytics;

    private static final int RC_OCR_CAPTURE = 9003;
    public Button button, button2, button3;
    public ScrollView result;
    public String kenteken;
    private ConnectionDetector connection;
    public KentekenHandler Khandler;
    private static AdView mAdView;
    private Bundle bundle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        final EditText text = findViewById(R.id.kenteken);

        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (text.getText().length() == 6) {
                    String temp = KentekenHandler.formatLicenseplate(text.getText().toString());
                    if (!text.getText().toString().equals(temp)) {
                        text.setText(temp);
                    }
                }

                if (text.getText().toString().replace("-", "").length() > 6) {
                    text.setText(text.getText().toString().substring(0,6));
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

        button = findViewById(R.id.sendRequest);
        button2 = findViewById(R.id.camera);
        button3 = findViewById(R.id.recent);

        result = findViewById(R.id.scroll);
        connection = new ConnectionDetector(this);

        getAds();

        Khandler = new KentekenHandler(MainActivity.this, connection, button3, result, text, mAdView);

        button.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        button2.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        button3.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));

        performPermissionCheck();

        button.setOnClickListener(
                new View.OnClickListener()

                {
                    @Override
                    public void onClick(View v) {
                        Khandler.run(text);
                    }
                });
        button2.setOnClickListener(
                new View.OnClickListener()

                {
                    @Override
                    public void onClick(View v) {
                        startCameraIntent();
                    }
                });
        button3.setOnClickListener(
                new View.OnClickListener()

                {
                    @Override
                    public void onClick(View v) {
                        Khandler.openRecent();
                    }
                });
        text.setOnKeyListener(new View.OnKeyListener()

        {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:
                            Khandler.run(text);
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
    }

    protected void getAds() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
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
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, bundle);
                }

                @Override
                public void onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void performPermissionCheck(){

    }
}