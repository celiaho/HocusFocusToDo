package edu.bhcc.cho.hocusfocustodo.data.model

import com.google.gson.annotations.SerializedName

data class SignupRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("extra") val extra: String? = null  // ? = optional field
)