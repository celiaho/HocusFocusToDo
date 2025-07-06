package edu.bhcc.cho.hocusfocustodo.data.network

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import edu.bhcc.cho.hocusfocustodo.LoginRequest
import edu.bhcc.cho.hocusfocustodo.SignupRequest
import edu.bhcc.cho.hocusfocustodo.utils.SessionManager
import edu.bhcc.cho.hocusfocustodo.utils.VolleySingleton
import org.json.JSONObject

class AuthApiService(private val context: Context) {
    private val baseUrl = "http://10.0.2.2:8080"
    private val requestQueue = VolleySingleton.getInstance(context).requestQueue

    /**
     * Logs in a user and returns the JWT token string.
     */
    fun loginUser(
        request: LoginRequest,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$baseUrl/auth/login"
        val json = JSONObject().apply {
            put("email", request.email)
            put("password", request.password)
        }

        val req = JsonObjectRequest(Request.Method.POST, url, json,
            { response ->
                val token = response.optString("token", "")
                if (token.isNotEmpty()) onSuccess(token)
                else onError("Missing token in response")
            },
            { error -> onError(error.message ?: "Login failed") }
        )

        requestQueue.add(req)
    }

    /**
     * Signs up a user and returns the full response object.
     */
    fun signupUser(
        request: SignupRequest,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$baseUrl/auth/signup"
        val json = JSONObject().apply {
            put("email", request.email)
            put("password", request.password)
            put("first_name", request.firstName)
            put("last_name", request.lastName)
            put("extra", request.extra)
        }

        val req = JsonObjectRequest(Request.Method.POST, url, json,
            { response -> onSuccess(response) },
            { error -> onError(error.message ?: "Signup failed") }
        )

        requestQueue.add(req)
    }

    /**
     * Sends a password reset request for the given email address.
     * Triggers the server to generate and store an OTP (one-time password)
     * for the user to use in the password reset process.
     */
    fun requestPasswordReset(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$baseUrl/auth/forgot-password"
        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        val requestBody = jsonBody.toString()

        val request = object : StringRequest(Method.POST, url,
            { onSuccess() },
            { error -> onError("Failed to request reset: ${error.message}") }
        ) {
            override fun getBody(): ByteArray {
                return requestBody.toByteArray(Charsets.UTF_8)
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        requestQueue.add(request)
    }

    /**
     * Uses OTP to reset a user's password.
     */
    fun resetPassword(
        email: String,
        newPassword: String,
        otp: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Log.d("AuthApiService---", "---Calling updated resetPassword() using StringRequest")

        val url = "$baseUrl/auth/reset-password"
        val jsonBody = JSONObject().apply {
            put("email", email)
            put("new_password", newPassword)
            put("otp", otp)
        }

        val request = object : StringRequest(Method.POST, url,
            { onSuccess() }, // no response body to parse
            { error -> onError("Reset failed: ${error.message}") }
        ) {
            override fun getBody(): ByteArray = jsonBody.toString().toByteArray(Charsets.UTF_8)
            override fun getBodyContentType(): String = "application/json"
        }

        requestQueue.add(request)
    }

    /**
     * Fetches the current user's profile using JWT in header.
     */
    fun getMyProfile(
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$baseUrl/profiles/me"
        val req = object : JsonObjectRequest(Method.GET, url, null,
            { response -> onSuccess(response) },
            { error -> onError(error.message ?: "Profile fetch failed") }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf("Authorization" to "Bearer ${SessionManager(context).getToken()}")
            }
            // ⚠️TEST CODE to see if Volley automatically retries failed profile fetches 1 time-- it does.
//        }.apply {
//            // Prevent retry on failure (especially for 401)
//            retryPolicy = DefaultRetryPolicy(
//                0, // initial timeout in ms
//                0, // no retries
//                1f // backoff multiplier
//            )
        }

        requestQueue.add(req)
    }

    //⚠️TEST CODE: Workaround to bypass /profiles/me on the server since server rejects JWTs as expired regardless of exp timestamp
    fun getProfileById(
        userId: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$baseUrl/profiles/$userId"
        val req = object : JsonObjectRequest(Method.GET, url, null,
            { response -> onSuccess(response) },
            { error -> onError(error.message ?: "Profile fetch failed") }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf("Authorization" to "Bearer ${SessionManager(context).getToken()}")
            }
        }

        requestQueue.add(req)
    }
}