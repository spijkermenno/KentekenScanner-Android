package com.MennoSpijker.kentekenscanner.Font;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Menno on 11/12/2017.
 */

public class FontManager {

    public static final String ROOT = "";
    private static String FONTAWESOME;

    public static String setIconType(IconType iconType) {
        if (iconType.equals(IconType.REGULAR)) {
            FONTAWESOME = ROOT + "Font Awesome 5 Free-Regular-400.otf";
        } else {
            FONTAWESOME = ROOT + "Font Awesome 5 Free-Solid-900.otf";
        }

        return FONTAWESOME;
    }

    public static Typeface getTypeface(Context context, String font) {
        return Typeface.createFromAsset(context.getAssets(), font);
    }

    public static void markAsIconContainer(View v, Typeface typeface) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                markAsIconContainer(child, typeface);
            }
        } else if (v instanceof TextView) {
            ((TextView) v).setTypeface(typeface);
        }
    }
}

