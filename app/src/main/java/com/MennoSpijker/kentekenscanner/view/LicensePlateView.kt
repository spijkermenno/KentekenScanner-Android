package com.MennoSpijker.kentekenscanner.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.MennoSpijker.kentekenscanner.R


class LicensePlateView(context: Context, private val attrs: AttributeSet) :
    LinearLayout(context, attrs) {
    init {
        inflate(context, R.layout.license_plate_container, this)
    }
}

class LicensePlateDetailsView(context: Context, attrs: AttributeSet) :
    ConstraintLayout(context, attrs) {
    private var viewModel: LicensePlateViewModel
    private var licensePlateAdapter: LicensePlateAdapter

    init {
        inflate(context, R.layout.personalization_carousel, this)

        viewModel = LicensePlateViewModel()

        licensePlateAdapter = LicensePlateAdapter()
    }
}

