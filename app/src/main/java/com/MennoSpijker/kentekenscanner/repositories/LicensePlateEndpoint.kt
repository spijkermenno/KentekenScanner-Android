package com.MennoSpijker.kentekenscanner.repositories

import com.MennoSpijker.kentekenscanner.responses.LicensePlateResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface LicensePlateEndpoint {
    @GET("kenteken/{uuid}/")
    fun getLicensePlatesForUUID(
        @Path("uuid") uuid: String
    ): Call<ArrayList<LicensePlateResponse>>

    @GET("kenteken/full/{uuid}/{licenseplate_id}")
    fun getLicensePlatesWithUUIDAndLicenseplateID(
        @Path("uuid") uuid: String,
        @Path("licenseplate_id") licenseplateId: Int
    ): Call<LicensePlateResponse>

    @GET("kenteken/{uuid}/{licenseplate}/")
    fun requestNewLicensePlateData(
        @Path("licenseplate") licenseplate: String,
        @Path("uuid") uuid: String
    ): Call<Int>

    @Multipart
    @POST("kenteken/image/{licenseplate}")
    fun upload(
        @Path("licenseplate") licenseplate: String,
        @Part file: MultipartBody.Part?
    ): Call<ResponseBody>
}