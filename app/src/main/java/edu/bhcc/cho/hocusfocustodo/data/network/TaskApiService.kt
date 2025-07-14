package edu.bhcc.cho.hocusfocustodo.data.network

import android.content.Context
import android.util.Log
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import edu.bhcc.cho.hocusfocustodo.data.model.Task
import edu.bhcc.cho.hocusfocustodo.utils.SessionManager
import edu.bhcc.cho.hocusfocustodo.utils.VolleySingleton
import org.json.JSONArray
import org.json.JSONObject

class TaskApiService(context: Context) {
    private val requestQueue = VolleySingleton.getInstance(context).requestQueue
    private val baseUrl = "http://10.0.2.2:8080"
    private val sessionManager = SessionManager(context)

    private fun authHeaders(): Map<String, String> =
        mapOf("Authorization" to "Bearer ${sessionManager.getToken()}")

    fun getAllTasks(
        documentId: String,
        onSuccess: (Map<String, List<Task>>) -> Unit,
        onError: (VolleyError) -> Unit
    ) {
        val url = "$baseUrl/documents/$documentId"

        val request = object : JsonObjectRequest(Method.GET, url, null,
            { response ->
                try {
                    val content = response.getJSONObject("content")
                    val taskMap = mutableMapOf<String, List<Task>>()

                    listOf("q1_u_i", "q2_nu_i", "q3_u_ni", "q4_nu_ni").forEach { quadrant ->
                        val array = content.optJSONArray(quadrant) ?: JSONArray()
                        taskMap[quadrant] = parseTasks(array, quadrant)
                    }

                    onSuccess(taskMap)
                } catch (e: Exception) {
                    Log.e("TaskApiService", "Failed to parse quadrant tasks: ${e.message}")
                    onError(VolleyError("Parse error"))
                }
            },
            { error ->
                Log.e("TaskApiService", "Error fetching document: ${error.message}")
                onError(error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> = authHeaders().toMutableMap()
        }

        requestQueue.add(request)
    }

    fun saveAllTasks(
        documentId: String,
        taskMap: Map<String, List<Task>>,
        onSuccess: () -> Unit,
        onError: (VolleyError) -> Unit
    ) {
        val url = "$baseUrl/documents/$documentId"
        val payload = JSONObject()
        val content = JSONObject()

        taskMap.forEach { (quadrant, tasks) ->
            val taskArray = JSONArray()
            tasks.forEach {
                taskArray.put(JSONObject().apply {
                    put("id", it.id)
                    put("text", it.text)
                    put("isCompleted", it.isCompleted)
                    it.dueDate?.let { due -> put("dueDate", due) }
                })
            }
            content.put(quadrant, taskArray)
        }

        payload.put("content", content)

        val request = object : JsonObjectRequest(Method.PUT, url, payload,
            { _ -> onSuccess() },
            { error -> onError(error) }
        ) {
            override fun getHeaders(): MutableMap<String, String> = authHeaders().toMutableMap()
        }

        requestQueue.add(request)
    }

    fun createNewDocument(
        taskMap: Map<String, List<Task>>,
        onSuccess: (String) -> Unit,
        onError: (VolleyError) -> Unit
    ) {
        val url = "$baseUrl/documents"
        val content = JSONObject()

        taskMap.forEach { (quadrant, tasks) ->
            val taskArray = JSONArray()
            tasks.forEach { task ->
                val taskJson = JSONObject().apply {
                    put("id", task.id)
                    put("text", task.text)
                    put("isCompleted", task.isCompleted)
                    task.dueDate?.let { put("dueDate", it) }
                }
                taskArray.put(taskJson)
            }
            content.put(quadrant, taskArray)
        }

        val payload = JSONObject().put("content", content)

        val request = object : JsonObjectRequest(Method.POST, url, payload,
            { response ->
                val docId = response.optString("id", null)
                if (docId != null) {
                    onSuccess(docId)
                } else {
                    Log.e("TaskApiService", "Missing document ID in create response")
                    onError(VolleyError("Missing document ID"))
                }
            },
            { error ->
                Log.e("TaskApiService", "Error creating document: ${error.message}")
                onError(error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> = authHeaders().toMutableMap()
        }

        requestQueue.add(request)
    }

    fun deleteTask(
        taskId: String,
        onSuccess: () -> Unit,
        onError: (VolleyError) -> Unit
    ) {
        val url = "$baseUrl/tasks/$taskId"
        val request = object : StringRequest(Method.DELETE, url,
            { _ -> onSuccess() },
            { error -> onError(error) }
        ) {
            override fun getHeaders(): MutableMap<String, String> = authHeaders().toMutableMap()
        }

        requestQueue.add(request)
    }

    private fun parseTasks(array: JSONArray, quadrant: String): List<Task> {
        val list = mutableListOf<Task>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val task = Task(
                id = obj.getString("id"),
                text = obj.getString("text"),
                isCompleted = obj.optBoolean("isCompleted", false),
                quadrant = quadrant,
                dueDate = obj.optString("dueDate", null)
            )
            list.add(task)
        }
        return list
    }
}
