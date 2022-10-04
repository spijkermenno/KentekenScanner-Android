package com.MennoSpijker.kentekenscanner.models

import com.google.gson.annotations.SerializedName

data class LicensePlateDetails (
    @SerializedName("key")
    var key: String? = null,

    @SerializedName("content")
    var content: String? = null
)
