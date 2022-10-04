package com.MennoSpijker.kentekenscanner.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.MennoSpijker.kentekenscanner.repositories.LicensePlateRepository
import com.MennoSpijker.kentekenscanner.responses.LicensePlateResponse
import com.google.firebase.analytics.FirebaseAnalytics

class LicensePlateViewModel(val context: Context) {
    val list: MutableLiveData<LicensePlateResponse> = MutableLiveData()
    val bundle: Bundle = Bundle()

    fun getLicensePlateDetails(licensePlate: String) {
        bundle.putString(
            FirebaseAnalytics.Param.ITEM_NAME,
            KentekenHandler.formatLicensePlate(licensePlate)
        )
        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.SEARCH, bundle)

        LicensePlateRepository.getLicensePlateDetails(licensePlate) { licensePlateResponse ->
            this.list.value = licensePlateResponse
        }
    }

}