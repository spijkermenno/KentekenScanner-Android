package com.MennoSpijker.kentekenscanner.repositories

import android.util.Log
import com.MennoSpijker.kentekenscanner.view.RestService
import com.MennoSpijker.kentekenscanner.responses.LicensePlateResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object LicensePlateRepository {

    fun getLicensePlateDetails(licensePlate: String, callback: (LicensePlateResponse?) -> Unit) {
        val request = RestService.getLicensePlateEndPoint().getLicensePlateDetails(licensePlate)

        request.enqueue(object : Callback<LicensePlateResponse> {
            override fun onResponse(
                call: Call<LicensePlateResponse>,
                response: Response<LicensePlateResponse>
            ) {
                if (response.isSuccessful) {
                    callback(response.body())
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<LicensePlateResponse>, t: Throwable) {
                t.printStackTrace()
                callback(null)
            }
        })
    }
}

