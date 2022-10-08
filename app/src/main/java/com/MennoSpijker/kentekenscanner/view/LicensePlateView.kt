package com.MennoSpijker.kentekenscanner.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.MennoSpijker.kentekenscanner.R

class LicensePlateView(context: Context, private val attrs: AttributeSet) :
    LinearLayout(context, attrs) {
    init {
        inflate(context, R.layout.license_plate_container, this)
    }
}

