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

    fun uploadFile(licenseplate: String, body: MultipartBody.Part?, callback: (Int) -> Unit) {
        val call = RestService.getLicensePlateEndPoint().upload(licenseplate, body)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                callback(response.code())
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
                callback(-1)
            }
        })
    }

    fun addLicensePlateToUUID(licensePlate: String, uuid: String, callback: (Int) -> Unit) {
        val request =
            RestService.getLicensePlateEndPoint().requestNewLicensePlateData(licensePlate, uuid)

        request.enqueue(object : Callback<Int> {
            override fun onResponse(
                call: Call<Int>,
                response: Response<Int>
            ) {
                response.body()?.let(callback) ?: callback(-1)
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                t.printStackTrace()
                callback(-1)
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
                callback(response.body())
            }

            override fun onFailure(call: Call<LicensePlateResponse>, t: Throwable) {
                t.printStackTrace()
                callback(null)
            }
        })
    }

    fun getLicensePlatesForUUID(uuid: String, callback: (ArrayList<LicensePlateResponse>) -> Unit) {
        val request = RestService.getLicensePlateEndPoint().getLicensePlatesForUUID(uuid)

        request.enqueue(object : Callback<ArrayList<LicensePlateResponse>> {
            override fun onResponse(
                call: Call<ArrayList<LicensePlateResponse>>,
                response: Response<ArrayList<LicensePlateResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    callback(response.body()!!)
                } else {
                    callback(ArrayList())
                }
            }

            override fun onFailure(call: Call<ArrayList<LicensePlateResponse>>, t: Throwable) {
                t.printStackTrace()
                callback(ArrayList())
            }
        })
    }
}

