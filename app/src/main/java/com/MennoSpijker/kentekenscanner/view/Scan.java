package com.MennoSpijker.kentekenscanner.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.MennoSpijker.kentekenscanner.ConnectionDetector;
import com.MennoSpijker.kentekenscanner.FontManager;
import com.MennoSpijker.kentekenscanner.OcrCaptureActivity;
import com.MennoSpijker.kentekenscanner.R;
import com.MennoSpijker.kentekenscanner.Request;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.AdView;
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

        //MobileAds.initialize(this, "ca-app-pub-4928043878967484~7828914059");

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                System.out.println(initializationStatus);
            }
        });

        try {

            AdView adView = new AdView(this);

            adView.setAdUnitId("ca-app-pub-4928043878967484/5146910390");
            mAdView = this.findViewById(R.id.ad1);
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

        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (text.getText().length() == 6) {
                    String temp = kentekenHandler.formatLicenseplate(text.getText().toString());
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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