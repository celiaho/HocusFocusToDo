package edu.bhcc.cho.hocusfocustodo.data.model

import com.google.gson.annotations.SerializedName

data class APIError(
    @SerializedName("error") val error: String
)
