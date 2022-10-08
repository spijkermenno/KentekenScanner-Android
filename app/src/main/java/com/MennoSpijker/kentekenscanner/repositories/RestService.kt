package com.MennoSpijker.kentekenscanner.repositories

import com.MennoSpijker.kentekenscanner.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RestService {
    private var licensePlateEndpoint: LicensePlateEndpoint? = null

    fun getLicensePlateEndPoint(): LicensePlateEndpoint {
        if (licensePlateEndpoint == null) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.ENDPOINT_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            licensePlateEndpoint = retrofit.create(LicensePlateEndpoint::class.java)
        }

        return licensePlateEndpoint!!
    }
}