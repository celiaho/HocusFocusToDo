package edu.bhcc.cho.hocusfocustodo.utils
import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class VolleySingleton constructor(context: Context) {
    // Companion object acts like a static holder in Java to ensure only one instance
    companion object {
        @Volatile   // Ensures INSTANCE is always up-to-date across threads
        private var INSTANCE: VolleySingleton? = null

        // Fn to get the singleton instance: if it doesn't exist, create it.
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {    // synchronized block for thread safety
                INSTANCE ?: VolleySingleton(context).also {     // Create and assign
                    INSTANCE = it
                }
            }
    }

    // The request queue; only initialized when first accessed per "by lazy"
    val requestQueue: RequestQueue by lazy {
        // applicationContext prevents leaking the Activity or BroadcastReceiver if someone passes one in
        Volley.newRequestQueue(context.applicationContext)
    }

    // Fn to add a request to the queue
    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }
}