package com.MennoSpijker.kentekenscanner.View;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

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

import static android.view.View.*;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    public FirebaseAnalytics mFirebaseAnalytics;

    private static final int RC_OCR_CAPTURE = 9003;
    public Button showHistoryButton, openCameraButton, showFavoritesButton, showAlertsButton;
    public ScrollView resultScrollView;
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
        connection = new ConnectionDetector(this);

        getAds();

        Khandler = new KentekenHandler(MainActivity.this, connection, showFavoritesButton, resultScrollView, kentekenTextField, mAdView);

        showHistoryButton.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        showHistoryButton.setTextSize(20);
        openCameraButton.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        openCameraButton.setTextSize(20);
        showFavoritesButton.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        showFavoritesButton.setTextSize(20);
        showAlertsButton.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        showAlertsButton.setTextSize(20);

        performPermissionCheck();

        showHistoryButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Khandler.openRecent();
            }
        });

        openCameraButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startCameraIntent();
                    }
                });

        showFavoritesButton.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        Khandler.openSaved();
                    }
                });

        showAlertsButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                //Khandler.openSaved();
            }
        });
        
        kentekenTextField.setOnKeyListener(new OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(KentekenHandler.getSidecodeLicenseplate(kentekenTextField.getText().toString().toUpperCase()) != -1 && KentekenHandler.getSidecodeLicenseplate(kentekenTextField.getText().toString().toUpperCase()) != -2) {
                    Log.d(TAG, "onKey: BINGO");
                    Khandler.run(kentekenTextField);
                    return true;
                } 
                
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:
                            Khandler.run(kentekenTextField);
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
                    mFirebaseAnalytics.logEvent("AD_CLICK", bundle);
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
        Log.d(TAG, "onRequestPermissionsResult: " + requestCode);
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

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG,"Permission is granted");
        } else {
            Log.v(TAG,"Permission is revoked");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

//            int permissionCheckWriteToStorage = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
//            int permissionCheckReadFromStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
//
//
//            if (permissionCheckWriteToStorage != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 1);
//            }
//            if (permissionCheckReadFromStorage != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
//            }
     }
}