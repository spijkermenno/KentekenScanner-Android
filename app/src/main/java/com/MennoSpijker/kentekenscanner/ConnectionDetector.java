package com.MennoSpijker.kentekenscanner;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Menno Spijker on 08/12/2017.
 */

public class ConnectionDetector {

    private final Context _context;

    public ConnectionDetector(Context context) {
        this._context = context;
    }

    public boolean isConnectingToInternet() {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null)
                    for (NetworkInfo networkInfo : info)
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }

            }
        }catch (Exception E){
            E.printStackTrace();
        }
        return false;
    }
}