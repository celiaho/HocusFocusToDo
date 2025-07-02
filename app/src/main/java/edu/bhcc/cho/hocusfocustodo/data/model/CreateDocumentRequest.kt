package edu.bhcc.cho.hocusfocustodo

import com.google.gson.annotations.SerializedName

data class CreateDocumentRequest(
    @SerializedName("content") val content: Map<String, Any> // Content can be any valid JSON
)
