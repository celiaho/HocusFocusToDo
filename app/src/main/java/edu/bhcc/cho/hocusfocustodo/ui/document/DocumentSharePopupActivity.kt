package edu.bhcc.cho.hocusfocustodo.ui.document

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import edu.bhcc.cho.hocusfocustodo.R
import edu.bhcc.cho.hocusfocustodo.data.network.DocumentApiService
import edu.bhcc.cho.hocusfocustodo.utils.SessionManager

// Resolve View and ViewGroup ambiguity
import android.view.View as AndroidView
import android.view.ViewGroup as AndroidViewGroup

class DocumentSharePopupActivity : AppCompatActivity() {

    private lateinit var closeButton: ImageButton
    private lateinit var noUsersText: TextView
    private lateinit var userListView: ListView
    private lateinit var adapter: ArrayAdapter<String>

    private lateinit var apiService: DocumentApiService
    private lateinit var sessionManager: SessionManager

    private var allUsers: MutableList<DocumentApiService.UserProfile> = mutableListOf()
    private var sharedUserIds: MutableSet<String> = mutableSetOf()

    private var documentId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_share_popup)

        // Get document ID from intent extras
        documentId = intent.getStringExtra("DOCUMENT_ID") ?: ""

        // Init views and services
        closeButton = findViewById(R.id.close_button)
        noUsersText = findViewById<TextView>(R.id.text_no_users)
        userListView = findViewById(R.id.listViewUsers)
        apiService = DocumentApiService(this)
        sessionManager = SessionManager(this)

        Log.d("---DOCUMENT_SHARE_POPUP_LOADED", "---DOCUMENT_SHARE_POPUP_LOADED")

        // Configure popup window
        window.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            (resources.displayMetrics.heightPixels * 0.8).toInt()
        )
        window.setBackgroundDrawableResource(android.R.color.transparent)

        // Handle close button click
        closeButton.setOnClickListener {
            finish() // Return to DocumentActivity.kt
        }

        // Start fetching users
        fetchSharableUsers()
    }

    private fun fetchSharableUsers() {
        val currentUserId = sessionManager.getUserId() ?: ""
        Log.d("---CURRENT_USER_ID", "---" + currentUserId)

        apiService.getAllUsers(
            onSuccess = { users ->
                Log.d("---RAW_USERS_RECEIVED", "---" + users.joinToString("\n") { "${it.firstName} ${it.lastName} (${it.id})" })
                allUsers = users.filter { user -> user.id != currentUserId }.toMutableList()
                fetchCurrentlySharedUsers()
                Log.d("---USERS_FETCHED", "---USERS FETCHED =" + allUsers.joinToString(", "))
            },
            onError = { error ->
                Log.e("---USER_FETCH_ERROR", "---ERROR FETCHING ALL USERS = " + error)
                showToast(error)
            }
        )
    }

    private fun fetchCurrentlySharedUsers() {
        apiService.getSharedUsers(
            documentId,
            onSuccess = { ids ->
                sharedUserIds = ids.toMutableSet()
                Log.d("---FETCHED_SHARED_USER_IDS", "---FETCHED SHARED USER IDS = " + sharedUserIds.joinToString(", "))
                renderUserList()
            },
            onError = { error ->
                Log.e("---SHARED_USER_FETCH_ERROR", "---ERROR FETCHING SHARED USERS = " + error)
                if (error.contains("403") || error.contains("Forbidden")) {
                    showToast("Only the document owner can modify sharing.")
                } else {
                    showToast(error)
                }
            }
        )
    }

    private fun renderUserList() {
        if (allUsers.isEmpty()) {
            // Hide the list and show empty message
            userListView.visibility = View.GONE
            noUsersText.visibility = View.VISIBLE
            return
        } else {
            userListView.visibility = View.VISIBLE
            noUsersText.visibility = View.GONE
        }

        adapter = object : ArrayAdapter<String>(
            this,
            R.layout.item_user_list_entry,
            allUsers.map { "${it.firstName} ${it.lastName} (${it.email})" }
        ) {
            override fun getView(position: Int, convertView: AndroidView?, parent: AndroidViewGroup): AndroidView {
                val view = super.getView(position, convertView, parent)
                val user = allUsers[position]
                view.setBackgroundColor(
                    if (sharedUserIds.contains(user.id)) {
                        ContextCompat.getColor(context, R.color.orange)
                    } else {
                        ContextCompat.getColor(context, android.R.color.transparent)
                    }
                )
                return view
            }
        }

        userListView.adapter = adapter

        userListView.setOnItemClickListener { _, _, position, _ ->
            val user = allUsers[position]
            if (sharedUserIds.contains(user.id)) {
                apiService.unshareDocumentWithUser(documentId, user.id,
                    onSuccess = {
                        Log.d("---DOCUMENT_UNSHARED", "---DOCUMENT UNSHARED WITH USER IDS " + sharedUserIds.joinToString(", "))
                        sharedUserIds.remove(user.id)
                        renderUserList()
                        showToast("Unshared with: ${user.firstName} ${user.lastName}")
                    },
                    onError = {
                        Log.e("---DOCUMENT_UNSHARE_ERROR", "---DOCUMENT UNSHARE ERROR = " + it)
                        showToast(it)
                    }
                )
            } else {
                apiService.shareDocumentWithUser(documentId, user.id,
                    onSuccess = {
                        Log.d("---DOCUMENT_SHARED", "---DOCUMENT SHARED WITH USER IDS " + sharedUserIds.joinToString(", "))
                        sharedUserIds.add(user.id)
                        renderUserList()
                        showToast("Shared with: ${user.firstName} ${user.lastName}")
                    },
                    onError = {
                        Log.e("---DOCUMENT_SHARE_ERROR", "---DOCUMENT SHARE ERROR = " + it)
                        showToast(it)
                    }
                )
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}