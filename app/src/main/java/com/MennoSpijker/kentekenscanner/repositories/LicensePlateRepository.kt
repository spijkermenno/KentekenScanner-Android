package com.MennoSpijker.kentekenscanner.repositories

import android.util.Log
import com.MennoSpijker.kentekenscanner.BuildConfig
import com.MennoSpijker.kentekenscanner.responses.LicensePlateResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object LicensePlateRepository {

    fun addLicensePlateToUUID(licensePlate: String, uuid: String, callback: () -> Unit) {
        Log.d("TAG", "addLicensePlateToUUID: $uuid $licensePlate")
        Log.d("TAG", "addLicensePlateToUUID: ${BuildConfig.ENDPOINT_API}kenteken/$uuid/$licensePlate/")
        val request = RestService.getLicensePlateEndPoint().requestNewLicensePlateData(licensePlate, uuid)

        request.enqueue(object : Callback<ArrayList<Any>> {
            override fun onResponse(
                call: Call<ArrayList<Any>>,
                response: Response<ArrayList<Any>>
            ) {
               callback()
            }

            override fun onFailure(call: Call<ArrayList<Any>>, t: Throwable) {
                t.printStackTrace()
                callback()
            }
        })
    }

    fun getLicensePlatesForUUID(uuid: String, callback: (ArrayList<LicensePlateResponse>) -> Unit) {
        Log.d("TAG", "getLicensePlatesForUUID: $uuid")
        val request = RestService.getLicensePlateEndPoint().getLicensePlatesForUUID(uuid)

        request.enqueue(object : Callback<ArrayList<LicensePlateResponse>> {
            override fun onResponse(
                call: Call<ArrayList<LicensePlateResponse>>,
                response: Response<ArrayList<LicensePlateResponse>>
            ) {
                Log.d("TAG", "getLicensePlatesForUUID UUID: $response")
                if (response.isSuccessful && response.body() != null) {
                    callback(response.body()!!)
                } else {
                    callback(ArrayList())
                }
            }

            override fun onFailure(call: Call<ArrayList<LicensePlateResponse>>, t: Throwable) {
                Log.d("TAG", "getLicensePlatesForUUID error: ${t.message}")
                t.printStackTrace()
                callback(ArrayList())
            }
        })
    }
}

