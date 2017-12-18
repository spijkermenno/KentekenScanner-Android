package com.y_gap.menno.kentekenscanner;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;

import com.google.android.gms.ads.*;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.common.api.CommonStatusCodes;

import org.json.JSONArray;

import java.net.URL;

import java.util.ArrayList;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class Scan extends Activity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private static final String TAG = "Scan";
    private static final String AUTHOR = "Author => Menno Spijker";
    private static final int RC_OCR_CAPTURE = 9003;
    private static final int MY_PERMISSIONS_REQUEST_READ_MEDIA = 1;
    private static final String NAMEFILE = "data.json";
    public Button button, button2, button3;
    public ScrollView result;
    public Async runner;
    public Request response;
    public EditText text;
    public String kenteken;
    public String uri;
    public URL url;
    private ConnectionDetector connection;
    private Context main;
    public JSONArray arr;
    public kentekenHandler Khandler;
    public int ads;
    public boolean firstAdReceived;

    final String BUILD = "1002";

    ArrayList<String> shownKeys = new ArrayList<String>();
    ArrayList<String> resultList = new ArrayList<String>();

    private static AdView mAdView;
    private Context context;
    private ImageView imageView;

    protected void getAds() {

        MobileAds.initialize(this, "ca-app-pub-4928043878967484~7828914059");

        try {

            AdView adView = new AdView(this);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId("ca-app-pub-4928043878967484~7828914059");

            mAdView = this.findViewById(R.id.ad1);

            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    mAdView.setVisibility(View.VISIBLE);
                    ads = 1;
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    if (ads != 1) {
                        try {
                            mAdView.setVisibility(View.GONE);
                            ads = 0;
                            throw new Exception("Geen internet");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onAdOpened() {
                }

                @Override
                public void onAdLeftApplication() {
                }

                @Override
                public void onAdClosed() {
                    AdRequest adRequest = new AdRequest.Builder().build();
                    mAdView.loadAd(adRequest);
                }
            });
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        final EditText text = findViewById(R.id.kenteken);

        button = findViewById(R.id.sendRequest);
        button2 = findViewById(R.id.camera);
        button3 = findViewById(R.id.recent);

        result = findViewById(R.id.scroll);
        connection = new ConnectionDetector(this);

        getAds();

        Khandler = new kentekenHandler(Scan.this, connection, button3, result, text, mAdView);

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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(Scan.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void performPermissionCheck(){
        int permissionCheck = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
        int permissionCheck2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);


        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_MEDIA);
        }
        if (permissionCheck2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_MEDIA);
        }
    }
}