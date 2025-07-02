package edu.bhcc.cho.hocusfocustodo

import com.google.gson.annotations.SerializedName

data class APIError(
    @SerializedName("error") val error: String
)
