package com.MennoSpijker.kentekenscanner.Factory;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.MennoSpijker.kentekenscanner.View.MainActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class GoogleAdFactory {
    private final String publishID = "ca-app-pub-4928043878967484/5146910390";
    private final MainActivity context;

    public GoogleAdFactory(MainActivity activity) {
        this.context = activity;

        // initizing ads
        MobileAds.initialize(activity, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                System.out.println("Ad InitializationStatus: " + initializationStatus);
            }
        });
    }

    public AdView createBanner(AdSize size) {
        AdView adView = new AdView(context);

        adView.setAdSize(size);
        adView.setAdUnitId(publishID);

        adView.setBackgroundColor(Color.parseColor("#ffffff"));

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Bundle bundle = new Bundle();
                context.mFirebaseAnalytics.logEvent("Ad_loaded", bundle);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                Bundle bundle = new Bundle();
                bundle.putString("Message", adError.getMessage());
                context.mFirebaseAnalytics.logEvent("Ad_error", bundle);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                Bundle bundle = new Bundle();
                context.mFirebaseAnalytics.logEvent("AD_CLICK", bundle);
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

        return adView;
    }
}
