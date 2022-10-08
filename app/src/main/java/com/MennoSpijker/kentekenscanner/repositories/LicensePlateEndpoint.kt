package com.MennoSpijker.kentekenscanner.repositories

import com.MennoSpijker.kentekenscanner.responses.LicensePlateResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.GET
import retrofit2.http.Path

interface LicensePlateEndpoint {
    @GET("kenteken/{uuid}/")
    fun getLicensePlatesForUUID(
        @Path("uuid") uuid: String
    ): Call<ArrayList<LicensePlateResponse>>

    @GET("kenteken/{uuid}/{licenseplate}/")
    fun requestNewLicensePlateData(
        @Path("licenseplate") licenseplate: String,
        @Path("uuid") uuid: String
    ): Call<ArrayList<Any>>
}