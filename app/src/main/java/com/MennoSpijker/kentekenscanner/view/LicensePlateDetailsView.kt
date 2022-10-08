package com.MennoSpijker.kentekenscanner.view

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.MennoSpijker.kentekenscanner.R
import com.MennoSpijker.kentekenscanner.adapter.LicensePlateAdapter
import com.MennoSpijker.kentekenscanner.viewmodel.LicensePlateViewModel

class LicensePlateDetailsView(context: Context, attrs: AttributeSet) :
    ConstraintLayout(context, attrs) {
    private var viewModel: LicensePlateViewModel
    private var licensePlateAdapter: LicensePlateAdapter

    init {
        inflate(context, R.layout.personalization_carousel, this)

        viewModel = LicensePlateViewModel(context)

        licensePlateAdapter = LicensePlateAdapter()
    }
}