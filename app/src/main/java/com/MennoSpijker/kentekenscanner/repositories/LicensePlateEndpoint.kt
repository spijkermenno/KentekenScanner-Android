package com.MennoSpijker.kentekenscanner.repositories

import com.MennoSpijker.kentekenscanner.responses.LicensePlateResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface LicensePlateEndpoint {
    @GET("kenteken/{licenseplate}")
    fun getLicensePlateDetails(
        @Path("licenseplate") licenseplate: String
    ): Call<LicensePlateResponse>
}