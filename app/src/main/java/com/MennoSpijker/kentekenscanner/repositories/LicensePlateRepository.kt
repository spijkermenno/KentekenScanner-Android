package com.MennoSpijker.kentekenscanner.repositories

import android.util.Log
import com.MennoSpijker.kentekenscanner.BuildConfig
import com.MennoSpijker.kentekenscanner.responses.LicensePlateResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object LicensePlateRepository {

    fun uploadFile(licenseplate: String, body: MultipartBody.Part?, callback: (Boolean) -> Unit) {
        val call = RestService.getLicensePlateEndPoint().upload(licenseplate, body)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("TAG", "upload: onResponse: success")
                callback(true)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("TAG", "upload: onFailure: ${t.message}")
                t.printStackTrace()
                callback(false)
            }
        })
    }

    fun addLicensePlateToUUID(licensePlate: String, uuid: String, callback: () -> Unit) {
        Log.d("TAG", "addLicensePlateToUUID: $uuid $licensePlate")
        Log.d(
            "TAG",
            "addLicensePlateToUUID: ${BuildConfig.ENDPOINT_API}kenteken/$uuid/$licensePlate/"
        )
        val request =
            RestService.getLicensePlateEndPoint().requestNewLicensePlateData(licensePlate, uuid)

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

    fun getLicensePlatesWithUUIDAndLicenseplateID(
        uuid: String,
        licenseplateId: Int,
        callback: (LicensePlateResponse?) -> Unit
    ) {
        val request = RestService.getLicensePlateEndPoint()
            .getLicensePlatesWithUUIDAndLicenseplateID(uuid, licenseplateId)

        request.enqueue(object : Callback<LicensePlateResponse> {
            override fun onResponse(
                call: Call<LicensePlateResponse>,
                response: Response<LicensePlateResponse>
            ) {
                Log.d("TAG", "onResponse: $response")
                callback(response.body())
            }

            override fun onFailure(call: Call<LicensePlateResponse>, t: Throwable) {
                t.printStackTrace()
                callback(null)
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

