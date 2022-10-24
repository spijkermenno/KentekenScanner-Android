package com.MennoSpijker.kentekenscanner.viewmodel

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import com.MennoSpijker.kentekenscanner.Util.KentekenHandler
import com.MennoSpijker.kentekenscanner.activity.MainActivity
import com.MennoSpijker.kentekenscanner.repositories.LicensePlateRepository
import com.MennoSpijker.kentekenscanner.responses.LicensePlateResponse
import com.google.firebase.analytics.FirebaseAnalytics
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


class LicensePlateViewModel(val context: Context) {
    val list: MutableLiveData<ArrayList<LicensePlateResponse>> = MutableLiveData()
    val bundle: Bundle = Bundle()

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

        Log.d("TAG", "addLicensePlateToUUID: $licencePlateReadyForRequest")

        LicensePlateRepository.addLicensePlateToUUID(licencePlateReadyForRequest, uuid) {
            LicensePlateRepository.getLicensePlatesForUUID(uuid) {
                this.list.value = it;
            }
        }
    }

    fun getLicensePlateForUUID(uuid: String) {
        bundle.putString(
            "UUID",
            uuid
        )
        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.LOGIN, bundle)

        Log.d("TAG", "getLicensePlatesForUUID: $uuid")

        LicensePlateRepository.getLicensePlatesForUUID(uuid) {
            this.list.value = it;
        }
    }

    fun getLicensePlatesWithUUIDAndLicenseplateID(
        uuid: String,
        licenseplateId: Int
    ) {
        LicensePlateRepository.getLicensePlatesWithUUIDAndLicenseplateID(uuid, licenseplateId) {
            Log.d("TAG", "getLicensePlatesWithUUIDAndLicenseplateID: $it")
            it?.let {
                this.list.value = arrayListOf(it)
            }
        }
    }

    fun uploadImage(licensePlate: String) {
    }


}