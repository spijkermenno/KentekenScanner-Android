package com.MennoSpijker.kentekenscanner.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.MennoSpijker.kentekenscanner.Util.KentekenHandler
import com.MennoSpijker.kentekenscanner.activity.LicensePlateDetailsActivity
import com.MennoSpijker.kentekenscanner.repositories.LicensePlateRepository
import com.MennoSpijker.kentekenscanner.responses.LicensePlateResponse
import com.google.firebase.analytics.FirebaseAnalytics

class LicensePlateViewModel(val context: Context) {
    val list: MutableLiveData<ArrayList<LicensePlateResponse>> = MutableLiveData()
    private val bundle: Bundle = Bundle()

    fun addLicensePlateToUUID(licensePlate: String, uuid: String) {
        bundle.putString(
            FirebaseAnalytics.Param.ITEM_NAME,
            KentekenHandler.formatLicensePlate(licensePlate)
        )

        var licencePlateReadyForRequest: String = licensePlate
        licencePlateReadyForRequest = licencePlateReadyForRequest.replace("-", "")
        licencePlateReadyForRequest = licencePlateReadyForRequest.replace(" ", "")
        licencePlateReadyForRequest = licencePlateReadyForRequest.replace("\n", "")

        licencePlateReadyForRequest = licencePlateReadyForRequest.uppercase()

        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.SEARCH, bundle)

        LicensePlateRepository.addLicensePlateToUUID(licencePlateReadyForRequest, uuid) {
            val intent = Intent(context, LicensePlateDetailsActivity::class.java)
            intent.putExtra("licenseplateID", it)
            intent.putExtra("licenseplate", licencePlateReadyForRequest)
            ContextCompat.startActivity(context, intent, null)
        }
    }

    fun getLicensePlateForUUID(uuid: String) {
        bundle.putString(
            "UUID",
            uuid
        )

        LicensePlateRepository.getLicensePlatesForUUID(uuid) {
            this.list.value = it;
        }
    }

    fun getLicensePlatesWithUUIDAndLicenseplateID(
        uuid: String,
        licenseplateId: Int
    ) {
        LicensePlateRepository.getLicensePlatesWithUUIDAndLicenseplateID(uuid, licenseplateId) {
            it?.let {
                this.list.value = arrayListOf(it)
            }
        }
    }

    fun uploadImage(licensePlate: String) {
    }


}