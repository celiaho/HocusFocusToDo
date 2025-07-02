package edu.bhcc.cho.hocusfocustodo

import com.google.gson.annotations.SerializedName

data class UpdateDocumentRequest(
    @SerializedName("content") val content: Map<String, Any> // Content can be any valid JSON
)
