package com.MennoSpijker.kentekenscanner.responses

import com.MennoSpijker.kentekenscanner.models.LicensePlateDetails
import com.google.gson.annotations.SerializedName

data class LicensePlateResponse(
    @SerializedName("licenseplate")
    val licensePlate: String,

    @SerializedName("imageURL")
    val image: String,

    @SerializedName("details")
    val details: ArrayList<LicensePlateDetails>
)
