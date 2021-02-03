package com.MennoSpijker.kentekenscanner.View;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.MennoSpijker.kentekenscanner.ConnectionDetector;
import com.MennoSpijker.kentekenscanner.Factory.GoogleAdFactory;
import com.MennoSpijker.kentekenscanner.Factory.NotificationPublisher;
import com.MennoSpijker.kentekenscanner.FontManager;
import com.MennoSpijker.kentekenscanner.OcrCaptureActivity;
import com.MennoSpijker.kentekenscanner.R;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.firebase.analytics.FirebaseAnalytics;

import static android.view.View.OnClickListener;
import static android.view.View.OnKeyListener;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final boolean DEBUG = true;
    public FirebaseAnalytics mFirebaseAnalytics;

    private static final int RC_OCR_CAPTURE = 9003;
    public Button searchButton, openCameraButton, openRecents, openSaved;
    public ScrollView resultScrollView;
    public String kenteken;
    private ConnectionDetector connection;
    public SearchHandler Khandler;
    private static AdView mAdView;
    private Bundle bundle;
    public GoogleAdFactory factory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        factory = new GoogleAdFactory(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        createNotificationChannel();

        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        final EditText kentekenTextField = findViewById(R.id.kenteken);

        kentekenTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (kentekenTextField.getText().length() == 6) {
                    String formatedLicenceplate = SearchHandler.formatLicenseplate(kentekenTextField.getText().toString());
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

        searchButton = findViewById(R.id.sendRequest);
        openCameraButton = findViewById(R.id.camera);
        openRecents = findViewById(R.id.recent);
        openSaved = findViewById(R.id.saved);

        resultScrollView = findViewById(R.id.scroll);
        connection = new ConnectionDetector(this);

        if (!DEBUG) {
            //resultScrollView.removeAllViews();
            resultScrollView.addView(factory.createBanner(AdSize.BANNER));
        }

        Khandler = new SearchHandler(MainActivity.this, connection, openRecents, resultScrollView, kentekenTextField, mAdView);

        searchButton.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        openCameraButton.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        openRecents.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        openSaved.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));

        performPermissionCheck();

        // Setting Button handlers.

        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Khandler.run(kentekenTextField);
            }
        });

        openCameraButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startCameraIntent();
            }
        });

        openRecents.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Khandler.openRecent();
            }
        });

        openSaved.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Khandler.openSaved();
            }
        });
        kentekenTextField.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
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

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String notificationMessage = extras.getString("kenteken", "UNDEFINED");
            if (!notificationMessage.equals("UNDEFINED")) {
                Khandler.runIntent(kentekenTextField, notificationMessage);
            }
        } else {
            System.out.println("No extra's");
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NotificationPublisher.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
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
        if (requestCode == 1) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void closeKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }


    public void performPermissionCheck() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission is granted");
        } else {
            Log.v(TAG, "Permission is revoked");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }
}