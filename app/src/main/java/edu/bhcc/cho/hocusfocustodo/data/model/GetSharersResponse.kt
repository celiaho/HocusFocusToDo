package edu.bhcc.cho.hocusfocustodo

import com.google.gson.annotations.SerializedName

data class GetSharersResponse(
    @SerializedName("shared_with") val sharedWith: List<String> // List of Profile IDs (dashless)
)
