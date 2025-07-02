package edu.bhcc.cho.hocusfocustodo.ui.document

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.bhcc.cho.hocusfocustodo.R
import edu.bhcc.cho.hocusfocustodo.utils.SessionManager
import edu.bhcc.cho.hocusfocustodo.data.network.DocumentApiService
import edu.bhcc.cho.hocusfocustodo.data.model.Document
import edu.bhcc.cho.hocusfocustodo.ui.auth.LoginActivity
import edu.bhcc.cho.hocusfocustodo.ui.settings.SettingsActivity

class DocumentManagementActivity : AppCompatActivity() {
    private val editDocLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data?.getBooleanExtra(
                "REFRESH_NEEDED",
                false
            ) == true
        ) {
            loadDocuments()
        }
    }

    // Declare tab buttons
    private lateinit var tabMyFiles: Button
    private lateinit var tabSharedFiles: Button

    // Declare sections
    private lateinit var layoutMyFiles: LinearLayout
    private lateinit var layoutSharedFiles: LinearLayout

    // Declare RecyclerViews and empty states
    private lateinit var recyclerMyFiles: RecyclerView
    private lateinit var recyclerSharedFiles: RecyclerView
    private lateinit var emptyMyFiles: TextView
    private lateinit var emptySharedFiles: TextView

    // Declare DocumentAPIService
    private lateinit var apiService: DocumentApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_management)

        // Clear stale document cache on screen entry
        getSharedPreferences("DocumentCache", MODE_PRIVATE).edit { clear() }

        // Highlight Document Management icon in toolbar
        findViewById<ImageButton>(R.id.icon_open_folder)
            .setColorFilter(ContextCompat.getColor(this, R.color.orange))

        // *Handle toolbar clicks*
        // New Document Button
        findViewById<ImageButton>(R.id.icon_document).setOnClickListener {
            Log.d("---NEW_DOCUMENT_BUTTON_CLICKED", "---NEW_DOCUMENT_BUTTON_CLICKED")
            getSharedPreferences("DocumentCache", MODE_PRIVATE).edit { clear() } // Clear local cache
            // Open new document
            startActivity(Intent(this, DocumentActivity::class.java).apply {
                putExtra("newDoc", true)
            })
        }
        // Document Management Icon - Already here
        findViewById<ImageButton>(R.id.icon_open_folder).setOnClickListener {
            Toast.makeText(this, "You are already in File Management.", Toast.LENGTH_SHORT).show()
        }
        // Settings Icon
        findViewById<ImageButton>(R.id.icon_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        // Logout Icon
        findViewById<ImageButton>(R.id.icon_logout).setOnClickListener {
            getSharedPreferences("DocumentCache", MODE_PRIVATE).edit { clear() } // Clear local cache
            SessionManager(this).clearSession() // Clear token + userId
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Initialize API
        apiService = DocumentApiService(this)

        // Connect UI elements
        tabMyFiles = findViewById(R.id.tab_my_files)
        tabSharedFiles = findViewById(R.id.tab_shared_files)
        layoutMyFiles = findViewById(R.id.layout_my_files)
        layoutSharedFiles = findViewById(R.id.layout_shared_files)
        recyclerMyFiles = findViewById(R.id.recycler_my_files)
        recyclerSharedFiles = findViewById(R.id.recycler_shared_files)
        emptyMyFiles = findViewById(R.id.empty_my_files)
        emptySharedFiles = findViewById(R.id.empty_shared_files)

        Log.d("---DOCUMENT_MANAGEMENT_PAGE_LOADED", "---DOCUMENT_MANAGEMENT_PAGE_LOADED")

        // Set up Recycler Layout Views/layout managers
        recyclerMyFiles.layoutManager = LinearLayoutManager(this)
        recyclerSharedFiles.layoutManager = LinearLayoutManager(this)

        // *Handle tab clicks*
        // My Files tab
        tabMyFiles.setOnClickListener {
            Log.d("---MY_FILES_TAB_CLICKED", "---MY_FILES_TAB_CLICKED")
            tabMyFiles.setBackgroundColor(getColor(R.color.blue))
            tabMyFiles.setTextColor(getColor(R.color.white))
            tabSharedFiles.setBackgroundColor(getColor(R.color.light_gray))
            tabSharedFiles.setTextColor(getColor(R.color.dark_gray))
            layoutMyFiles.visibility = View.VISIBLE
            layoutSharedFiles.visibility = View.GONE
        }
        // Shared Files tab
        tabSharedFiles.setOnClickListener {
            Log.d("---SHARED_FILES_TAB_CLICKED", "---SHARED_FILES_TAB_CLICKED")
            tabSharedFiles.setBackgroundColor(getColor(R.color.blue))
            tabSharedFiles.setTextColor(getColor(R.color.white))
            tabMyFiles.setBackgroundColor(getColor(R.color.light_gray))
            tabMyFiles.setTextColor(getColor(R.color.dark_gray))
            layoutSharedFiles.visibility = View.VISIBLE
            layoutMyFiles.visibility = View.GONE
        }

        // Patch: Clear stale cache before loading documents
        getSharedPreferences("DocumentCache", MODE_PRIVATE).edit { clear() }
        Log.d("---DOCUMENT_CACHE_CLEARED", "---DOCUMENT_CACHE_CLEARED")

        // Fetch and display documents from the server
        loadDocuments()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val refresh = data?.getBooleanExtra("REFRESH_NEEDED", false) == true
            if (refresh) {
                Log.d("---REFRESHING_DOC_LIST", "---Detected document change, reloading list")
                loadDocuments()
            }
        }
    }

    /**
     * Loads documents from the backend API and updates the corresponding views.
     */
    private fun loadDocuments() {
        apiService.getDocuments(
            onSuccess = { documents ->
                // Log API response to verify that document list has loaded
                Log.d("---DOCUMENTS_RECEIVED", "---Loaded ${documents.size} documents")
                documents.forEach { doc ->
                    Log.d(
                        "---DOC",
                        "---ID=${doc.id}, title=${doc.title}, modified=${doc.lastModifiedDate}"
                    )
                }

                // Get users to map owner names
                apiService.getAllUsers(
                    onSuccess = { users ->
                        val currentUserId = SessionManager(this).getUserId()

                        // Split My Files (owned) vs Shared With Me tab (shared)
                        val myFiles = documents.filter { it.ownerId == currentUserId }
                        val sharedFiles = documents.filter { it.ownerId != currentUserId }
                        updateTabs(myFiles, sharedFiles, users)
                    },
                    onError = {
                        Log.e("---LOAD_USERS_ERROR", "---Error loading users: $it")
                        Toast.makeText(this, "Error loading users", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onError = { error ->
                Log.e("---LOAD_DOCUMENTS_ERROR", "---Error loading documents: $error")
                Toast.makeText(this, "Error loading documents", Toast.LENGTH_SHORT).show()
                emptyMyFiles.visibility = View.VISIBLE
                emptySharedFiles.visibility = View.VISIBLE
            }
        )
    }

    /**
     * Helper to update My Files and Shared Files tabs after documents and shares are loaded.
     */
    private fun updateTabs(
        myFiles: List<Document>,
        sharedFiles: List<Document>,
        users: List<DocumentApiService.UserProfile>
    ) {
        // Pass users to adapters so Owner labels can be mapped
        recyclerMyFiles.adapter = DocumentAdapter(this, myFiles, editDocLauncher::launch, users)
        recyclerSharedFiles.adapter =
            DocumentAdapter(this, sharedFiles, editDocLauncher::launch, users)

        // Show/hide empty views for My Files
        if (myFiles.isEmpty()) {
            recyclerMyFiles.visibility = View.GONE
            emptyMyFiles.visibility = View.VISIBLE
        } else {
            recyclerMyFiles.visibility = View.VISIBLE
            emptyMyFiles.visibility = View.GONE
        }

        // Show/hide empty views for Shared Files
        if (sharedFiles.isEmpty()) {
            recyclerSharedFiles.visibility = View.GONE
            emptySharedFiles.visibility = View.VISIBLE
        } else {
            recyclerSharedFiles.visibility = View.VISIBLE
            emptySharedFiles.visibility = View.GONE
        }
    }
}