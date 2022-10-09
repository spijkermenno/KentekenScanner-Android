package com.MennoSpijker.kentekenscanner.responses

import com.MennoSpijker.kentekenscanner.models.LicensePlateDetails
import com.google.gson.annotations.SerializedName

class LicensePlateResponse (
    @SerializedName("id")
    val id: Int,

    @SerializedName("licenseplate")
    val licensePlate: String,

    @SerializedName("images")
    val images: ArrayList<String>,

    @SerializedName("days_till_apk")
    val daysTillAPK: Int?,

    @SerializedName("details")
    val details: ArrayList<LicensePlateDetails>
): RecyclerViewItem()
