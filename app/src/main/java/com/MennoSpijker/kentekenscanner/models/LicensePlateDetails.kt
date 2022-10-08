package com.MennoSpijker.kentekenscanner.models

import com.google.gson.annotations.SerializedName

data class LicensePlateDetails (
    @SerializedName("key")
    var key: String = "",

    @SerializedName("content")
    var content: String = ""
)
