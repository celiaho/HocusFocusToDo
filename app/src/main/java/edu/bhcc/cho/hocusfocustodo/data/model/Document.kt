package edu.bhcc.cho.hocusfocustodo

import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class Document(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String, // JSON string e.g. {"title": "...", "body": "..."}
    @SerializedName("owner_id") val ownerId: String,
    @SerializedName("creation_date") val creationDate: String,
    @SerializedName("last_modified_date") val lastModifiedDate: String,
    @SerializedName("shared_with") val sharedWith: List<String> = emptyList()
) {
    fun getParsedContent(): Pair<String, String> {
        return try {
            val json = JSONObject(content)
            val title = json.optString("title", "")
            val body = json.optString("body", "")
            Pair(title, body)
        } catch (e: Exception) {
            Pair("", "")
        }
    }
}