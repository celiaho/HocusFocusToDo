package edu.bhcc.cho.hocusfocustodo.data.network

import android.content.Context
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import edu.bhcc.cho.hocusfocustodo.data.model.Profile
import edu.bhcc.cho.hocusfocustodo.utils.SessionManager
import edu.bhcc.cho.hocusfocustodo.utils.VolleySingleton
import org.json.JSONArray
import org.json.JSONObject

/**
 * Handles profile-related network operations via the /profiles endpoint.
 */
class ProfileApiService(private val context: Context) {
    private val requestQueue = VolleySingleton.getInstance(context).requestQueue
    private val sessionManager = SessionManager(context)
    private val baseUrl = "http://10.0.2.2:8080"
    private val tag = "ProfileApiService"

    /**
     * Fetches the currently authenticated user's profile.
     */
    fun getProfile(onSuccess: (Profile) -> Unit, onError: (VolleyError) -> Unit) {
        val url = "$baseUrl/profiles/me"
        val request = object : JsonObjectRequest(Method.GET, url, null, { response ->
            try {
                val profile = parseProfileJson(response)
                onSuccess(profile)
            } catch (e: Exception) {
                onError(VolleyError("Error parsing profile: ${e.message}"))
            }
        }, { error -> onError(error) }) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf("Authorization" to "Bearer ${sessionManager.getToken()}")
            }
        }
        request.tag = tag
        requestQueue.add(request)
    }

    /**
     * Updates the authenticated user's profile.
     */
    fun updateProfile(
        updatedProfile: Profile,
        onSuccess: (JSONObject) -> Unit,
        onError: (VolleyError) -> Unit
    ) {
        val url = "$baseUrl/profiles/me"
        val jsonBody = JSONObject().apply {
            put("first_name", updatedProfile.firstName)
            put("last_name", updatedProfile.lastName)
            put("extra", JSONObject(updatedProfile.extra ?: emptyMap<String, Any>()))
        }

        val request = object : JsonObjectRequest(Method.PUT, url, jsonBody, onSuccess, onError) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf("Authorization" to "Bearer ${sessionManager.getToken()}")
            }
        }
        request.tag = tag
        requestQueue.add(request)
    }

    /**
     * Searches for profiles using a query string.
     */
    fun searchProfiles(
        query: String,
        onSuccess: (List<Profile>) -> Unit,
        onError: (VolleyError) -> Unit
    ) {
        val url = "$baseUrl/profiles?query=$query"
        val request = object : JsonArrayRequest(Method.GET, url, null, { response ->
            try {
                onSuccess(parseProfileList(response))
            } catch (e: Exception) {
                onError(VolleyError("Parse error: ${e.message}"))
            }
        }, { error -> onError(error) }) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf("Authorization" to "Bearer ${sessionManager.getToken()}")
            }
        }
        request.tag = tag
        requestQueue.add(request)
    }

    /**
     * Helper: Parse a profile object from JSON.
     */
    private fun parseProfileJson(jsonObject: JSONObject): Profile {
        val extraMap = mutableMapOf<String, Any>()
        jsonObject.optJSONObject("extra")?.let { extraJson ->
            val keys = extraJson.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                extraMap[key] = extraJson.get(key)
            }
        }

        return Profile(
            id = jsonObject.getString("id"),
            email = jsonObject.getString("email"),
            firstName = jsonObject.getString("first_name"),
            lastName = jsonObject.getString("last_name"),
            creationDate = jsonObject.getString("creation_date"),
            lastModifiedDate = jsonObject.getString("last_modified_date"),
            extra = extraMap
        )
    }

    /**
     * Helper: Parse a list of profiles from a JSON array.
     */
    private fun parseProfileList(jsonArray: JSONArray): List<Profile> {
        val profiles = mutableListOf<Profile>()
        for (i in 0 until jsonArray.length()) {
            val profileJson = jsonArray.getJSONObject(i)
            profiles.add(parseProfileJson(profileJson))
        }
        return profiles
    }

    /**
     * Cancels all Volley requests in the queue that match the given tag.
     * Can be used to clean up network requests when an activity is destroyed/a user logs out to
     * prevent memory leaks or unintended callbacks.
     *
     * @param tag The tag associated with the requests to cancel.
     */
    fun cancelRequests(tag: Any) {
        requestQueue.cancelAll(tag)
    }

    /**
     * Deletes the currently logged-in user's profile.
     * This action is irreversible and removes all owned data.
     */
    fun deleteProfile(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val url = "$baseUrl/profiles/me"
        val appContext = context

        val request = object : StringRequest(Method.DELETE, url,
            { onSuccess() },
            { error -> onError("Delete failed: ${error.message}") }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf("Authorization" to "Bearer ${SessionManager(appContext).getToken()}")
            }
        }

        requestQueue.add(request)
    }
}