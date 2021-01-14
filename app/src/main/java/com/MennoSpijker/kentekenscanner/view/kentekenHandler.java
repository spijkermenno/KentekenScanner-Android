package com.MennoSpijker.kentekenscanner.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.MennoSpijker.kentekenscanner.ConnectionDetector;
import com.MennoSpijker.kentekenscanner.FontManager;
import com.MennoSpijker.kentekenscanner.R;
import com.MennoSpijker.kentekenscanner.util.FileHandling;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class kentekenHandler {
    private static final String NAMEFILE = "data.json";
    private final Button button3;
    private final Context context;

    private final ConnectionDetector connection;
    private String uri;
    private Async runner;
    private final ScrollView result;
    private final TextView kentekenHolder;
    public AdView mAdView;

    public kentekenHandler(Context c, ConnectionDetector co, Button b, ScrollView r, TextView kHold, AdView mad) {
        this.context = c;
        this.connection = co;
        this.button3 = b;
        this.result = r;
        this.kentekenHolder = kHold;
        this.mAdView = mad;
    }

    public ArrayList<String> getRecentKenteken() {
        ArrayList<String> kentekens = new ArrayList<>();

        String fileContent = new FileHandling().readFile(context, NAMEFILE);

        try {
            JSONArray arr = new JSONArray(fileContent);
            JSONObject obj = arr.getJSONObject(0);
            JSONArray arr2 = obj.getJSONArray("kenteken");

            try {
                int myJsonArraySize = arr2.length();

                for (int i = 0; i < myJsonArraySize; i++) {
                    kentekens.add(arr2.get(i).toString());
                }
            } catch (NullPointerException NPE) {
                NPE.printStackTrace();
            }

        } catch (JSONException JE) {
            JE.printStackTrace();
        }
        return kentekens;
    }

    public void SaveKenteken(String kenteken) {
        ArrayList<String> otherKentekens = getRecentKenteken();

        new FileHandling().writeToFile(context, NAMEFILE, kenteken, otherKentekens);
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
            final ArrayList<String> recents = getRecentKenteken();

            LinearLayout lin = new LinearLayout(context);
            lin.setOrientation(LinearLayout.VERTICAL);

            for (String recent : recents) {
                recent = recent.replace("/", "");

                Button line = new Button(context);
                line.setText(kentekenHandler.formatLicenseplate(recent));
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

            if (recents.size() > 0) {
                Button clear = new Button(context);

                clear.setTypeface(FontManager.getTypeface(context, FontManager.FONTAWESOME));
                clear.setText(R.string.fa_icon_trash);
                clear.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new FileHandling().emptyFile(context, NAMEFILE);
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

    public void run(TextView textview) {
        String kenteken = textview.getText().toString().toUpperCase();
        runCamera(kenteken, textview);
    }

    public void runCamera(String kenteken, TextView textview) {
        try {
            kenteken = kenteken.replace("-", "");
            kenteken = kenteken.replace(" ", "");
            kenteken = kenteken.replace("\n", "");
            if (kenteken.length() > 0) {
                textview.setText(formatLicenseplate(kenteken));
                uri = "https://opendata.rdw.nl/resource/m9d7-ebf2.json?kenteken=" + kenteken;
                result.removeAllViews();
                runner = new Async(this.context, kenteken, result, uri, connection, button3, this, mAdView);
                runner.execute("1000");
            }
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }
        InputMethodManager inputManager = (InputMethodManager) this.context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public static String formatLicenseplate(String licenseplate) {

        try {
            int sidecode = getSidecodeLicenseplate(licenseplate);

            System.out.println("sidecode is: " + sidecode);

            licenseplate = licenseplate.replace("-", "").toUpperCase();

            if (sidecode <= 6) {
                return licenseplate.substring(0, 2) + '-' + licenseplate.substring(2, 4) + '-' + licenseplate.substring(4, 6);
            }
            if (sidecode == 7 || sidecode == 9) {
                return licenseplate.substring(0, 2) + '-' + licenseplate.substring(2, 5) + '-' + licenseplate.substring(5, 6);
            }
            if (sidecode == 8 || sidecode == 10) {
                return licenseplate.substring(0, 1) + '-' + licenseplate.substring(1, 4) + '-' + licenseplate.substring(4, 6);
            }
            if (sidecode == 11 || sidecode == 14) {
                return licenseplate.substring(0, 3) + '-' + licenseplate.substring(3, 5) + '-' + licenseplate.substring(5, 6);
            }
            if (sidecode == 12 || sidecode == 13) {
                return licenseplate.substring(0, 1) + '-' + licenseplate.substring(1, 3) + '-' + licenseplate.substring(3, 6);
            }

            // todo add sidecode 14

            System.out.println("licenceplate is: " + licenseplate);

            return licenseplate;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return licenseplate;
    }

    public static int getSidecodeLicenseplate(String licenseplate) throws SideCodeException {

        String[] patterns = new String[14];

        licenseplate = licenseplate.replace("-", "").toUpperCase();

        System.out.println(licenseplate);

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

        throw new SideCodeException(licenseplate);
    }

    private static class SideCodeException extends Exception {
        public SideCodeException(String licenseplate) {
            super("No sidecode found for licenceplate: " + licenseplate);
        }
    }
}
