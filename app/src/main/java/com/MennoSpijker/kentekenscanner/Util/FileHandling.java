package com.MennoSpijker.kentekenscanner.Util;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class FileHandling {
    private static final String TAG = "PERMISSION";
    private final String storageDir;
    private static final String RecentKentekensFile = "recent.json";
    private static final String NotificationsFile = "notifications.json";
    private static final String SavedKentekensFile = "favorites.json";

    public FileHandling(Context context) {
        this.storageDir = context.getFilesDir() + "/";
    }

    public String readFile(String filename) {
        FileInputStream fis;
        int n;
        StringBuilder fileContent = new StringBuilder();

        File file = new File(storageDir + filename);
        System.out.println("FILENAME: " + file);
        if (file.exists()) {
            Log.d(TAG, "readFile: File exists.");
            try {
                fis = new FileInputStream(file);

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

            Log.d(TAG, "readFile() returned: " + fileContent);
            return fileContent.toString();
        } else {
            Log.d(TAG, "readFile: File doesn't exist, try to create");
            try {
                file.createNewFile();
                writeToFile(filename, new JSONObject());
                return readFile(filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void writeToFileOnDate(String filename, String newKenteken, String newKentekenDate, JSONObject otherKentekens) {
        JSONObject mainObject = new JSONObject();

        File file = new File(storageDir + filename);
        if (file.exists()) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                // defining previous saved kentekens
                System.out.println("other kentekens: " + otherKentekens);
                JSONArray previousSavedData = new JSONArray().put(otherKentekens);

                int amountOfDates = previousSavedData.getJSONObject(0).length();

                // Making sure no kentekens are double saved.
                int proceed = 1;
                boolean dateChecked = false;
                if (amountOfDates > 0) {

                    Iterator<String> iterator = otherKentekens.keys();

                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        JSONArray values = new JSONArray(otherKentekens.getString(key));
                        System.out.println("main object at begin:" + mainObject);
                        System.out.println(values);
                        System.out.println(values.length());

                        if (key.equals(newKentekenDate)) {
                            dateChecked = true;
                            for (int i = 0; i < values.length(); i++) {
                                String value = values.getString(i);

                                JSONArray currentDateKentekens = new JSONArray();
                                JSONArray currentDateKentekensNew = new JSONArray();

                                SimpleDateFormat s = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                                String currentDate = s.parse(key).toString();

                                // retrieve dates saved
                                JSONArray kentekensSavedOnDate = otherKentekens.getJSONArray(key);
                                int amountOfKentekens = kentekensSavedOnDate.length();

                                // retrieve kentekens saved on certain date
                                for (int j = 0; j < amountOfKentekens; j++) {
                                    String previousKenteken = kentekensSavedOnDate.getString(j);

                                    if (previousKenteken.equals(newKenteken)) {
                                        proceed = 0;
                                    }
                                    currentDateKentekens.put(previousKenteken);
                                }

                                if (proceed == 1) {
                                    currentDateKentekensNew.put(newKenteken);
                                }

                                for (int x = 0; x < currentDateKentekens.length(); x++) {
                                    currentDateKentekensNew.put(currentDateKentekens.getString(x));
                                }

                                mainObject.put(key, currentDateKentekensNew);
                            }
                        } else {
                            if (!dateChecked) {
                                mainObject.put(newKentekenDate, new JSONArray().put(newKenteken));
                            }
                            mainObject.put(key, otherKentekens.getJSONArray(key));
                        }
                    }
                } else {
                    System.out.println("No dates saved.");
                    mainObject.put(newKentekenDate, new JSONArray().put(newKenteken));
                }

                System.out.println("before write: " + mainObject);
                fileOutputStream.write(mainObject.toString().getBytes());

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                System.out.println(file.createNewFile());
                writeToFileOnDate(filename, newKenteken, newKentekenDate, otherKentekens);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeToFile(String filename, String newKenteken, JSONObject otherKentekens) {
        JSONObject mainObject = new JSONObject();

        File file = new File(storageDir + filename);
        if (file.exists()) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                // Making sure no kentekens are double saved.
                int proceed = 1;
                boolean dateChecked = false;

                String key = "cars";

                if (otherKentekens.length() > 0) {
                    JSONArray values = new JSONArray(otherKentekens.getString(key));

                    if (values.length() == 0) {
                        System.out.println("add new one");
                    }

                    for (int i = 0; i < values.length(); i++) {
                        String value = values.getString(i);

                        JSONArray currentDateKentekens = new JSONArray();
                        JSONArray currentDateKentekensNew = new JSONArray();

                        // retrieve dates saved
                        JSONArray kentekensSavedOnDate = otherKentekens.getJSONArray(key);
                        int amountOfKentekens = kentekensSavedOnDate.length();

                        // retrieve kentekens saved on certain date
                        for (int j = 0; j < amountOfKentekens; j++) {
                            String previousKenteken = kentekensSavedOnDate.getString(j);

                            if (previousKenteken.equals(newKenteken)) {
                                proceed = 0;
                            }
                            currentDateKentekens.put(previousKenteken);
                        }

                        if (proceed == 1) {
                            currentDateKentekensNew.put(newKenteken);
                        }

                        for (int x = 0; x < currentDateKentekens.length(); x++) {
                            currentDateKentekensNew.put(currentDateKentekens.getString(x));
                        }

                        mainObject.put(key, currentDateKentekensNew);
                    }

                } else {
                    mainObject.put(key, new JSONArray().put(newKenteken));
                }
                System.out.println("before write: " + mainObject);
                fileOutputStream.write(mainObject.toString().getBytes());
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                System.out.println(file.createNewFile());
                writeToFile(filename, newKenteken, otherKentekens);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void emptyFile(String filename) {
        String empty = "";
        File file = new File(storageDir + filename);
        if (file.exists()) {
            try {
                FileOutputStream fOut = new FileOutputStream(file);
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

    public JSONObject getSavedKentekens() {
        ArrayList<JSONArray> kentekens = new ArrayList<>();

        String fileContent = readFile(SavedKentekensFile);

        JSONObject mainObject = new JSONObject();
        try {
            mainObject = new JSONObject(fileContent);
        } catch (JSONException e) {
            System.out.println("error empty mainObject");
        }

        //return kentekens;
        return mainObject;
    }

    public JSONObject getPendingNotifications() {
        ArrayList<JSONArray> notifications = new ArrayList<>();

        String fileContent = readFile(NotificationsFile);

        JSONObject mainObject = new JSONObject();
        try {
            mainObject = new JSONObject(fileContent);
        } catch (JSONException e) {
            System.out.println("error empty mainObject");
        }

        Log.d(TAG, "getPendingNotifications: " + mainObject);

        //return notifications;
        return mainObject;
    }

    public void saveNotificationsToFile(JSONArray notifications) {
        JSONObject mainObject = new JSONObject();

        File file = new File(storageDir + NotificationsFile);
        if (file.exists()) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                String key = "notifications";

                mainObject.put(key, notifications);

                fileOutputStream.write(mainObject.toString().getBytes());
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        } else {
            // generate file and re-call this method
            try {
                System.out.println(file.createNewFile());
                saveNotificationsToFile(notifications);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public JSONObject getRecentKenteken() {
        ArrayList<JSONArray> kentekens = new ArrayList<>();

        String fileContent = readFile(RecentKentekensFile);

        JSONObject mainObject = new JSONObject();
        try {
            mainObject = new JSONObject(fileContent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //return kentekens;
        return mainObject;
    }

    public void writeToFile(String savedKentekensFile, JSONObject otherKentekens) {
        JSONObject mainObject = new JSONObject();

        File file = new File(storageDir + savedKentekensFile);
        if (file.exists()) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                String key = "cars";

                if (otherKentekens.length() > 0) {
                    JSONArray values = new JSONArray(otherKentekens.getString(key));
                    mainObject.put(key, otherKentekens.getJSONArray("cars"));
                }

                System.out.println("before write: " + mainObject);
                fileOutputStream.write(mainObject.toString().getBytes());
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                System.out.println(file.createNewFile());
                writeToFile(savedKentekensFile, otherKentekens);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void addNotificationToFile(JSONObject notificationObject) {
        try {
            JSONObject notifications = getPendingNotifications();

            if (notifications.length() > 0) {
                JSONArray notificationsArray = notifications.getJSONArray("notifications");

                if (!doesNotificationExist(notificationObject.getString("kenteken"))) {
                    notificationsArray.put(notificationObject);
                    this.saveNotificationsToFile(notificationsArray);
                } else {
                    Log.d(TAG, "addNotificationToFile: Notification with this kenteken already saved.");
                }

                Log.d(TAG, "addNotificationToFile: " + notifications);
            } else {
                this.saveNotificationsToFile(new JSONArray().put(notificationObject));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean doesNotificationExist(String kenteken) {
        boolean doesExistInArray = false;
        JSONObject notifications = getPendingNotifications();
        Log.d(TAG, "doesNotificationExist: " + notifications);
        Log.d(TAG, "doesNotificationExist: " + notifications.length());

        if (notifications.length() == 0) {
            return false;
        }

        try {
            JSONArray notificationsArray = notifications.getJSONArray("notifications");

            for (int i = 0; i < notificationsArray.length(); i++) {
                if (notificationsArray.getJSONObject(i).getString("kenteken").equals(kenteken)) {
                    doesExistInArray = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return doesExistInArray;
    }

    public void cleanUpNotificationList() {
        JSONObject pendingNotifications = getPendingNotifications();

        if (pendingNotifications.length() == 0) {
            return;
        }

        try {
            JSONArray notificationsArray = pendingNotifications.getJSONArray("notifications");

            boolean changedArray = false;

            for (int i = 0; i < notificationsArray.length(); i++) {
                String notificationDate = notificationsArray.getJSONObject(i).getString("notificationDateNoFormat");
                Date date = new SimpleDateFormat("dd-MM-yy", Locale.GERMANY).parse(notificationDate);

                if(date.before(new Date())) {
                    Log.d(TAG, "cleanUpNotificationList: Date passed");
                    notificationsArray.remove(i);
                    changedArray = true;
                } else {
                    Log.d(TAG, "cleanUpNotificationList: Date not passed");
                }

                if (changedArray) {
                    this.saveNotificationsToFile(notificationsArray);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
