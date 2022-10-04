package com.MennoSpijker.kentekenscanner.view

import android.util.Log
import com.MennoSpijker.kentekenscanner.BuildConfig
import com.MennoSpijker.kentekenscanner.repositories.LicensePlateEndpoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.log

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