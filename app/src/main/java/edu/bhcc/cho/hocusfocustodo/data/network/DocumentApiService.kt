package edu.bhcc.cho.noteserver.data.network

import android.content.Context
import android.util.Log
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import edu.bhcc.cho.noteserver.data.model.Document
import edu.bhcc.cho.noteserver.utils.SessionManager
import edu.bhcc.cho.noteserver.utils.VolleySingleton
import org.json.JSONArray
import org.json.JSONObject

/**
 * Handles all document CRUD and content-query operations.
 */
class DocumentApiService(context: Context) {
    private val requestQueue = VolleySingleton.getInstance(context).requestQueue
    private val baseUrl = "http://10.0.2.2:8080"
    private val sessionManager = SessionManager(context)

    private fun authHeaders(): Map<String, String> =
        mapOf("Authorization" to "Bearer ${sessionManager.getToken()}")

    fun createDocument(
        content: JSONObject,
        onSuccess: (JSONObject) -> Unit,
        onError: (VolleyError) -> Unit
    ) {
        val url = "$baseUrl/documents"
        // API EXAMPLE: val url = "$baseUrl/documents?scope=owned&sort_by=last_modified_date&order=asc&page=1&limit=10"
        val request = object : JsonObjectRequest(Method.POST, url, content, onSuccess, onError) {
            override fun getHeaders(): MutableMap<String, String> = authHeaders().toMutableMap()
        }
        requestQueue.add(request)
    }

    /**
     * Retrieves a specific document by its ID from the server.
     *
     * @param documentId The ID of the document to retrieve.
     * @param onSuccess Callback when the document is successfully fetched.
     * @param onError Callback when an error occurs.
     */
    fun getDocumentById(
        documentId: String,
        onSuccess: (Document) -> Unit,
        onError: (VolleyError) -> Unit
    ) {
        val url = "$baseUrl/documents/$documentId"
        val request = object : JsonObjectRequest(Method.GET, url, null,
            { response ->
                try {
                    val content = response.getJSONObject("content")

                    val sharedArray = response.optJSONArray("shared_with") ?: JSONArray()
                    val sharedWith = mutableListOf<String>()
                    for (i in 0 until sharedArray.length()) {
                        sharedWith.add(sharedArray.getString(i))
                    }

                    val document = Document(
                        id = response.getString("id"),
                        ownerId = response.getString("owner_id"),
                        creationDate = response.getString("creation_date"),
                        lastModifiedDate = response.getString("last_modified_date"),
                        title = content.getString("title"),
                        content = content.getString("body"),
                        sharedWith = sharedWith
                    )
                    onSuccess(document)
                } catch (e: Exception) {
                    Log.e("---GET_DOC_BY_ID_PARSE", "---Error parsing document: ${e.message}")
                    onError(VolleyError("Parse error"))
                }
            },
            { error -> onError(error) }
        ) {
            override fun getHeaders(): MutableMap<String, String> = authHeaders().toMutableMap()
        }

        requestQueue.add(request)
    }

    fun updateDocument(
        documentId: String,
        content: JSONObject,
        onSuccess: (JSONObject) -> Unit,
        onError: (VolleyError) -> Unit
    ) {
        val url = "$baseUrl/documents/$documentId"
        val request = object : JsonObjectRequest(Method.PUT, url, content, onSuccess, onError) {
            override fun getHeaders(): MutableMap<String, String> = authHeaders().toMutableMap()
        }
        requestQueue.add(request)
    }

    fun deleteDocument(
        documentId: String,
        onSuccess: (String) -> Unit,
        onError: (VolleyError) -> Unit
    ) {
        val url = "$baseUrl/documents/$documentId"
        val request = object : StringRequest(Method.DELETE, url, onSuccess, onError) {
            override fun getHeaders(): MutableMap<String, String> = authHeaders().toMutableMap()
        }
        requestQueue.add(request)
    }

    fun getDocuments(
        onSuccess: (List<Document>) -> Unit,
        onError: (VolleyError) -> Unit
    ) {
        val url = "$baseUrl/documents"
        val request = object : JsonObjectRequest(Method.GET, url, null,
            { response ->
                Log.d("---DOCUMENTS_RAW", response.toString()) // ADD THIS LINE

                try {
                    val dataArray = response.optJSONArray("data") ?: JSONArray()
                    val docs = parseList(dataArray)

                    docs.forEach {
                        Log.d("---DOC", "Parsed = ${it.id}, title=${it.title}, content=${it.content}")
                    }

                    onSuccess(docs)
                    Log.d("---DOCUMENTS_FETCHED", "---DOCUMENTS FETCHED = " + docs.joinToString(", "))
                } catch (e: Exception) {
                    Log.e("---PARSE_ERROR", "Failed to parse documents: ${e.message}")
                    onSuccess(emptyList()) // Fallback to avoid crash
                }
            },
            { error ->
                onError(error)
                Log.e("---DOCUMENTS_FETCH_ERROR", "---ERROR FETCHING DOCUMENTS = " + error)
            }
        )
        {
            override fun getHeaders(): MutableMap<String, String> = authHeaders().toMutableMap()
        }
        requestQueue.add(request)
    }

    /**
     * Parses a JSON array of document objects into a list of [Document] instances.
     *
     * Each object in the array must have the following fields:
     * - "id": String — unique document ID
     * - "owner_id": String — ID of the document's owner
     * - "creation_date": String — ISO timestamp of when the document was created
     * - "last_modified_date": String — ISO timestamp of last modification
     * - "content": JSONObject containing:
     *      - "title": String — document title
     *      - "body": String — document content
     * - "shared_with": Array of user IDs (optional, can be empty)
     *
     * @param array The [JSONArray] to parse, typically received from the server response.
     * @return A [List] of [Document] objects with parsed fields.
     */
    private fun parseList(array: JSONArray): List<Document> {
        val list = mutableListOf<Document>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val content = obj.optJSONObject("content") ?: JSONObject()

            val sharedWithArray = obj.optJSONArray("shared_with") ?: JSONArray()
            val sharedWith = mutableListOf<String>()
            for (j in 0 until sharedWithArray.length()) {
                sharedWith.add(sharedWithArray.getString(j))
            }

            Log.d("---DOC", "Parsed = ${obj.getString("id")}, sharedWith=${sharedWith.joinToString(",")}")

            // Build document
            list.add(Document(
                id = obj.getString("id"),
                ownerId = obj.getString("owner_id"),
                creationDate = obj.getString("creation_date"),
                lastModifiedDate = obj.getString("last_modified_date"),
                title = content.optString("title", ""),
                content = content.optString("body", ""),
                sharedWith = sharedWith
            ))
        }
        return list
    }

    fun getAllUsers(
        onSuccess: (List<UserProfile>) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$baseUrl/profiles"
        val request = object : JsonObjectRequest(Method.GET, url, null,
            { response ->
                val userArray = response.optJSONArray("data") ?: JSONArray()
                val users = mutableListOf<UserProfile>()
                for (i in 0 until userArray.length()) {
                    val obj = userArray.getJSONObject(i)
                    users.add(
                        UserProfile(
                            id = obj.getString("id"),
                            firstName = obj.getString("first_name"),
                            lastName = obj.getString("last_name"),
                            email = obj.getString("email")
                        )
                    )
                }
                onSuccess(users)
            },
            { error -> onError(error.message ?: "Error fetching users") }
        ) {
            override fun getHeaders(): MutableMap<String, String> = authHeaders().toMutableMap()
        }
        requestQueue.add(request)
    }

    fun getSharedUsers(
        documentId: String,
        onSuccess: (List<String>) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$baseUrl/documents/$documentId/shares"

        val request = object : JsonObjectRequest(Method.GET, url, null,
            { response ->
                try {
                    // Check if response is object with "shared_with"
                    if (response.has("shared_with")) {
                        val sharedArray = response.getJSONArray("shared_with")
                        val sharedIds = mutableListOf<String>()
                        for (i in 0 until sharedArray.length()) {
                            sharedIds.add(sharedArray.getString(i))
                        }
                        Log.d("---SHARED_USERS_FETCHED", "---SHARED USERS FETCHED (OBJECT fallback) = " + sharedIds.joinToString(", "))
                        onSuccess(sharedIds)
                    } else {
                        Log.w("---SHARED_USERS_UNEXPECTED", "---Unexpected shared users response, treating as empty list")
                        onSuccess(emptyList()) // Unexpected format — fallback to empty list
                    }
                } catch (e: Exception) {
                    Log.e("---SHARED_USERS_PARSE_ERROR", "Error parsing shared user IDs: ${e.message}")
                    onError("Error parsing shared user IDs: ${e.message}")
                }
            },
            { error ->
                if (error.networkResponse?.statusCode == 404) {
                    Log.w("---SHARED_USERS_EMPTY", "---No share record found, treating as empty list")
                    onSuccess(emptyList()) // valid state--fallback to empty list
                } else {
                    Log.e("---SHARED_USERS_FETCH_ERROR", "---ERROR FETCHING SHARED USERS = " + error)
                    onError(error.message ?: "Error fetching shared users")
                }
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> = authHeaders().toMutableMap()
        }

        requestQueue.add(request)
    }

    fun shareDocumentWithUser(
        documentId: String,
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$baseUrl/documents/$documentId/shares/$userId"
        val request = object : StringRequest(Method.PUT, url, { onSuccess() }, { error ->
            onError(error.message ?: "Error sharing document")
        }) {
            override fun getHeaders(): MutableMap<String, String> = authHeaders().toMutableMap()
        }
        requestQueue.add(request)
    }

    fun unshareDocumentWithUser(
        documentId: String,
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$baseUrl/documents/$documentId/shares/$userId"
        val request = object : StringRequest(Method.DELETE, url, { onSuccess() }, { error ->
            onError(error.message ?: "Error unsharing document")
        }) {
            override fun getHeaders(): MutableMap<String, String> = authHeaders().toMutableMap()
        }
        requestQueue.add(request)
    }

    // Add this data class inside or outside the service if not already available
    data class UserProfile(val id: String, val firstName: String, val lastName: String, val email: String)
}