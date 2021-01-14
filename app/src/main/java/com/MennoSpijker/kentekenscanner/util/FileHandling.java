package com.MennoSpijker.kentekenscanner.util;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FileHandling {
    public String readFile(Context context, String filename) {
        FileInputStream fis;
        int n;
        StringBuilder fileContent = new StringBuilder();

        try {
            fis = context.openFileInput(filename);

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

        System.out.println(fileContent.toString());

        return fileContent.toString();
    }

    public void writeToFile(Context context, String filename, String newKenteken, ArrayList<String> otherKentekens) {
        JSONArray arr = new JSONArray();
        JSONArray arrobj = new JSONArray();
        JSONObject obj = new JSONObject();

        try {
            FileOutputStream fOut = context.openFileOutput(filename, Context.MODE_PRIVATE);
            try {
                try {
                    JSONArray jsontemp = new JSONArray(otherKentekens);
                    int myJsonArraySize = jsontemp.length();

                    int proceed = 1;
                    for (int i = 0; i < myJsonArraySize; i++) {
                        Object myObj = jsontemp.get(i);
                        if (myObj.toString().equals(newKenteken)) {
                            proceed = 0;
                        }
                        arrobj.put(myObj.toString());
                    }
                    if (proceed == 1) {
                        arrobj.put(newKenteken);
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

    public void emptyFile(Context context, String filename) {
        String empty = "";
        try {
            FileOutputStream fOut = context.openFileOutput(filename, Context.MODE_PRIVATE);
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
