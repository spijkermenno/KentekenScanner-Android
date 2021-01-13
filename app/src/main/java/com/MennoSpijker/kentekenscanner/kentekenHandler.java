package com.MennoSpijker.kentekenscanner;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class kentekenHandler {
    private static final String TAG = "kenteken scanner";
    private static final String NAMEFILE = "data.json";
    private final Button button3;
    private final Context context;
    private final ArrayList<String> shownKeys;
    private final ConnectionDetector connection;
    private String kenteken;
    private String uri;
    private Async runner;
    private final ScrollView result;
    private final TextView kentekenHolder;
    public AdView mAdView;

    kentekenHandler(Context c, ConnectionDetector co, Button b, ScrollView r, TextView kHold, AdView mad) {
        this.context = c;
        this.connection = co;
        this.button3 = b;
        this.result = r;
        this.kentekenHolder = kHold;
        this.mAdView = mad;

        shownKeys = new ArrayList<String>();
        createControlArray();
    }

    protected void createControlArray() {
        shownKeys.add("kenteken");
        shownKeys.add("merk");
        shownKeys.add("eerste_kleur");
        shownKeys.add("tweede_kleur");
        shownKeys.add("handelsbenaming");
        shownKeys.add("inrichting");
        shownKeys.add("openstaande_terugroepactie_indicator");
        shownKeys.add("vervaldatum_apk");
        shownKeys.add("wacht_op_keuren");
        shownKeys.add("zuinigheidslabel");
        shownKeys.add("datum_laatste_tenaamstelling");
    }

    public ArrayList<String> getRecentKenteken() {
        String temp = null;
        ArrayList<String> kentekens = null;
        kentekens = new ArrayList<String>();
        FileInputStream fis;
        int n;
        StringBuffer fileContent = new StringBuffer();

        try {
            fis = context.openFileInput(NAMEFILE);

            byte[] buffer = new byte[1024];

            try {
                while ((n = fis.read(buffer)) != -1) {
                    fileContent.append(new String(buffer, 0, n));
                }
            } catch (IOException IE) {
                IE.printStackTrace();
            }
        } catch (FileNotFoundException FNF) {
            FNF.printStackTrace();
        }

        try {
            JSONArray arr = new JSONArray(fileContent.toString());
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
        JSONArray arr = new JSONArray();
        JSONArray arrobj = new JSONArray();
        JSONObject obj = new JSONObject();
        ArrayList<String> otherKentekens = getRecentKenteken();

        try {
            FileOutputStream fOut = context.openFileOutput(NAMEFILE, Context.MODE_PRIVATE);
            try {
                try {

                    // todo: add previous kentekens
                    JSONArray jsontemp = new JSONArray(otherKentekens);
                    int myJsonArraySize = jsontemp.length();

                    int proceed = 1;
                    for (int i = 0; i < myJsonArraySize; i++) {
                        Object myObj = jsontemp.get(i);
                        if (myObj.toString().equals(kenteken)){
                            proceed = 0;
                        }
                        arrobj.put(myObj.toString());
                    }
                    if (proceed == 1) {
                        arrobj.put(kenteken);
                    }
                    obj.put("kenteken", arrobj);
                    arr.put(obj);
                } catch (JSONException JE) {
                    JE.printStackTrace();
                }
                fOut.write(arr.toString().getBytes());
                fOut.close();
            } catch (IOException IE) {
                IE.printStackTrace();
            }
        } catch (FileNotFoundException FNFE) {
            FNFE.printStackTrace();
        }
    }

    public void openRecent() {
        kentekenHolder.setText("");
        kentekenHolder.clearFocus();

        InputMethodManager inputManager = (InputMethodManager) this.context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

        result.removeAllViews();
        try {
            final ArrayList<String> recent = getRecentKenteken();

            LinearLayout lin = new LinearLayout(context);
            lin.setOrientation(LinearLayout.VERTICAL);

            if (recent.size() > 0) {
                Button clear = new Button(context);

                clear.setTypeface(FontManager.getTypeface(context, FontManager.FONTAWESOME));
                clear.setText(R.string.fa_icon_trash);
                clear.setOnClickListener(
                        new View.OnClickListener()

                        {
                            @Override
                            public void onClick(View v) {
                                ClearKentekens();
                                openRecent();
                            }
                        });
                lin.addView(clear);
            }

            int x = 0;
            for (String aRecent : recent) {
                aRecent = aRecent.replace("/", "");

                Button line = new Button(context);
                line.setText(kentekenHandler.makeKenteken(aRecent));
                final String recentA = aRecent;

                line.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                line.setOnClickListener(
                        new View.OnClickListener()

                        {
                            @Override
                            public void onClick(View v) {
                                runCamera(recentA, kentekenHolder);
                            }
                        });

                if (x % 2 == 0) {
                    line.setBackgroundColor(Color.LTGRAY);
                } else {
                    line.setBackgroundColor(Color.WHITE);
                }

                lin.addView(line);
                x++;
            }
            result.addView(lin);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static String makeKenteken(String input) {
        input = input.toUpperCase();
        int checkPrev = 0;
        int checkCur;
        String kenteken = "";
        char[] kentekenChar = new char[6];
        String[] parts = new String[3];

        for(int i = 0; i < 6; i++){
            kentekenChar[i] = input.charAt(i);
        }

        try { // try sidecode 8

            // format = 1-aaa-11

            // setting part 3;
            if (Character.isDigit(kentekenChar[4]) && Character.isDigit(kentekenChar[5])) {
                parts[2] = String.valueOf(kentekenChar[4]) + kentekenChar[5];
            }
            // setting part 1;
            if (Character.isDigit(kentekenChar[0]) && parts[2].length() == 2) {
                parts[0] = String.valueOf(kentekenChar[0]);
            }
            // setting part 2;
            if (parts[0].length() == 1 && parts[2].length() == 2) {
                parts[1] = String.valueOf(kentekenChar[1]) + kentekenChar[2] + kentekenChar[3];
                kenteken = parts[0] + "-" + parts[1] + "-" + parts[2];
            }
        } catch (Exception e) {
        }

        if (kenteken.length() == 0) {
            try { // try sidecode 7

                // format = 11-aaa-1

                // setting part 3;
                if (Character.isAlphabetic(kentekenChar[4]) && Character.isDigit(kentekenChar[5])) { // setting part 3 of the licenseplate.
                    parts[2] = String.valueOf(kentekenChar[5]);
                }
                // setting part 1;
                if (Character.isDigit(kentekenChar[0]) && Character.isDigit(kentekenChar[1]) && parts[2].length() == 1) { // setting part 1 of the licenseplate
                    parts[0] = String.valueOf(kentekenChar[0]) + kentekenChar[1];
                }
                // setting part 2;
                if (parts[0].length() == 2 && parts[2].length() == 1) { // setting part 2 of the licenseplate
                    parts[1] = String.valueOf(kentekenChar[2]) + kentekenChar[3] + kentekenChar[4];
                    kenteken = parts[0] + "-" + parts[1] + "-" + parts[2];
                }
            } catch (Exception e) {
            }
        }
        if (kenteken.length() == 0) {
            try { // try sidecode 7

                // format = aa-111-a

                // setting part 3;
                if (Character.isDigit(kentekenChar[4]) && Character.isAlphabetic(kentekenChar[5])) { // setting part 3 of the licenseplate.
                    parts[2] = String.valueOf(kentekenChar[5]);
                }
                // setting part 1;
                if (Character.isAlphabetic(kentekenChar[0]) && Character.isAlphabetic(kentekenChar[1]) && parts[2].length() == 1) { // setting part 1 of the licenseplate
                    parts[0] = String.valueOf(kentekenChar[0]) + kentekenChar[1];
                }
                // setting part 2;
                if (parts[0].length() == 2 && parts[2].length() == 1) { // setting part 2 of the licenseplate
                    parts[1] = String.valueOf(kentekenChar[2]) + kentekenChar[3] + kentekenChar[4];
                    kenteken = parts[0] + "-" + parts[1] + "-" + parts[2];
                }
            } catch (Exception e) {
            }
        }
        if (kenteken.length() == 0) {
            try { // try sidecode 6

                // format = 11-aa-aa

                // setting part 1;
                if (Character.isDigit(kentekenChar[0]) && Character.isDigit(kentekenChar[1])) { // setting part 1 of the licenseplate
                    parts[0] = String.valueOf(kentekenChar[0]) + kentekenChar[1];
                }
                // setting part 2
                if (Character.isAlphabetic(kentekenChar[2]) && Character.isAlphabetic(kentekenChar[3])) { // setting part 2 of the licenseplate
                    parts[1] = String.valueOf(kentekenChar[2]) + kentekenChar[3];
                }
                // setting part 3;
                if (Character.isAlphabetic(kentekenChar[4]) && Character.isAlphabetic(kentekenChar[5])) { // setting part 3 of the licenseplate.
                    parts[2] = String.valueOf(kentekenChar[4]) + kentekenChar[5];
                    kenteken = parts[0] + "-" + parts[1] + "-" + parts[2];
                }
            } catch (Exception e) {
            }
        }

        // TODO :: add more kenteken checks for sidecode 9 till 14

        if (kenteken.length() == 0) {
            kenteken = input;
        }

        return kenteken;
    }

    public void run(TextView textview) {
        try {
            kenteken = textview.getText().toString().toUpperCase();
            kenteken = kenteken.replace("-", "");
            kenteken = kenteken.replace(" ", "");
            kenteken = kenteken.replace("/n", "");

            if (kenteken.length() > 0) {
                textview.setText(makeKenteken(kenteken));
                uri = "https://opendata.rdw.nl/resource/m9d7-ebf2.json?kenteken=" + kenteken;
                result.removeAllViews();
                runner = new Async(this.context, kenteken, result, shownKeys, uri, connection, button3, this, mAdView);
                runner.execute("1000");
            }
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }
        InputMethodManager inputManager = (InputMethodManager) this.context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public void runCamera(String kenteken, TextView textview) {
        try {

            kenteken = kenteken.replace("-", "");
            kenteken = kenteken.replace(" ", "");
            kenteken = kenteken.replace("\n", "");
            if (kenteken.length() > 0) {

                textview.setText(makeKenteken(kenteken));
                uri = "https://opendata.rdw.nl/resource/m9d7-ebf2.json?kenteken=" + kenteken;

                result.removeAllViews();

                runner = new Async(this.context, kenteken, result, shownKeys, uri, connection, button3, this, mAdView);
                runner.execute("1000");
            }
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }
        InputMethodManager inputManager = (InputMethodManager) this.context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public void ClearKentekens() {
        String empty = "";
        try {
            FileOutputStream fOut = context.openFileOutput(NAMEFILE, Context.MODE_PRIVATE);
            try {
                fOut.write(empty.getBytes());
                fOut.close();
            } catch (IOException IE) {
                IE.printStackTrace();
            }
        } catch (FileNotFoundException FNFE) {
            FNFE.printStackTrace();
        }
    }

}
