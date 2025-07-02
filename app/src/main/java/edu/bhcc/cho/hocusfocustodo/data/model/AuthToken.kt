package edu.bhcc.cho.hocusfocustodo

import com.google.gson.annotations.SerializedName

data class AuthToken(
    @SerializedName("token") val token: String
)