package edu.bhcc.cho.hocusfocustodo.ui.task

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import edu.bhcc.cho.hocusfocustodo.R
import edu.bhcc.cho.hocusfocustodo.data.model.Task
import edu.bhcc.cho.hocusfocustodo.data.network.TaskApiService
import edu.bhcc.cho.hocusfocustodo.ui.auth.LoginActivity
import edu.bhcc.cho.hocusfocustodo.utils.SessionManager
import java.util.*

class TaskOverviewActivity : AppCompatActivity() {

    private lateinit var inputQ1: TextInputEditText
    private lateinit var inputQ2: TextInputEditText
    private lateinit var inputQ3: TextInputEditText
    private lateinit var inputQ4: TextInputEditText

    private lateinit var listQ1: RecyclerView
    private lateinit var listQ2: RecyclerView
    private lateinit var listQ3: RecyclerView
    private lateinit var listQ4: RecyclerView

    private lateinit var logoutButton: ImageButton

    private val q1Tasks = mutableListOf<Task>()
    private val q2Tasks = mutableListOf<Task>()
    private val q3Tasks = mutableListOf<Task>()
    private val q4Tasks = mutableListOf<Task>()

    private lateinit var adapterQ1: TaskAdapter
    private lateinit var adapterQ2: TaskAdapter
    private lateinit var adapterQ3: TaskAdapter
    private lateinit var adapterQ4: TaskAdapter

    private lateinit var taskApiService: TaskApiService

    private lateinit var sessionManager: SessionManager
    private var documentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_overview)

        sessionManager = SessionManager(this)
        documentId = sessionManager.getTaskDocumentId()

        taskApiService = TaskApiService(this)
        if (documentId != null) {
            loadTasksFromServer(documentId!!)
        }

        bindViews()
        setupRecyclerViews()
        setupListeners()
    }

    private fun bindViews() {
        inputQ1 = findViewById(R.id.input_q1)
        inputQ2 = findViewById(R.id.input_q2)
        inputQ3 = findViewById(R.id.input_q3)
        inputQ4 = findViewById(R.id.input_q4)

        listQ1 = findViewById(R.id.list_q1)
        listQ2 = findViewById(R.id.list_q2)
        listQ3 = findViewById(R.id.list_q3)
        listQ4 = findViewById(R.id.list_q4)

        logoutButton = findViewById<ImageButton>(R.id.button_logout)
    }

    private fun setupRecyclerViews() {
        adapterQ1 = TaskAdapter(q1Tasks, ::onDeleteTask, ::onToggleComplete)
        adapterQ2 = TaskAdapter(q2Tasks, ::onDeleteTask, ::onToggleComplete)
        adapterQ3 = TaskAdapter(q3Tasks, ::onDeleteTask, ::onToggleComplete)
        adapterQ4 = TaskAdapter(q4Tasks, ::onDeleteTask, ::onToggleComplete)

        listQ1.layoutManager = LinearLayoutManager(this)
        listQ1.adapter = adapterQ1

        listQ2.layoutManager = LinearLayoutManager(this)
        listQ2.adapter = adapterQ2

        listQ3.layoutManager = LinearLayoutManager(this)
        listQ3.adapter = adapterQ3

        listQ4.layoutManager = LinearLayoutManager(this)
        listQ4.adapter = adapterQ4
    }

    private fun setupListeners() {
        logoutButton.setOnClickListener {
            sessionManager.clearSession()  // 👈 Clears auth token, user ID, etc.

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        inputQ1.setOnEditorActionListener { _, _, _ ->
            handleTaskInput(inputQ1, q1Tasks, adapterQ1, "q1_u_i")
            true
        }

        inputQ2.setOnEditorActionListener { _, _, _ ->
            handleTaskInput(inputQ2, q2Tasks, adapterQ2, "q2_nu_i")
            true
        }

        inputQ3.setOnEditorActionListener { _, _, _ ->
            handleTaskInput(inputQ3, q3Tasks, adapterQ3, "q3_u_ni")
            true
        }

        inputQ4.setOnEditorActionListener { _, _, _ ->
            handleTaskInput(inputQ4, q4Tasks, adapterQ4, "q4_nu_ni")
            true
        }
    }

    private fun handleTaskInput(
        inputField: TextInputEditText,
        taskList: MutableList<Task>,
        adapter: TaskAdapter,
        quadrantCode: String
    ) {
        val text = inputField.text?.toString()?.trim()
        if (!text.isNullOrEmpty()) {
            val newTask = Task(
                id = UUID.randomUUID().toString(),
                text = text,
                isCompleted = false,
                quadrant = quadrantCode
            )
            taskList.add(newTask)
            adapter.notifyItemInserted(taskList.size - 1)
            inputField.text = null
            saveAllTasksToServer()
            hideKeyboard(inputField)  // 👈 Hide keyboard here
        }
    }

    private fun hideKeyboard(view: android.view.View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun saveAllTasksToServer() {
        val allTasks = mapOf(
            "q1_u_i" to q1Tasks,
            "q2_nu_i" to q2Tasks,
            "q3_u_ni" to q3Tasks,
            "q4_nu_ni" to q4Tasks
        )

        val taskApiService = TaskApiService(this)

        if (documentId == null) {
            // 🔥 Call createNewDocument because no document exists yet
            taskApiService.createNewDocument(
                taskMap = allTasks,
                onSuccess = { newId ->
                    // ✅ Success callback goes here
                    documentId = newId
                    sessionManager.saveTaskDocumentId(newId)

                    // Optional: immediately trigger save again now that you have a document ID
                    taskApiService.saveAllTasks(
                        documentId = newId,
                        taskMap = allTasks,
                        onSuccess = {
                            Log.d("SAVE_TASKS", "Initial task saved after doc creation.")
                        },
                        onError = {
                            Log.e("SAVE_TASKS", "Failed to save after doc creation: ${it.message}")
                        }
                    )
                },
                onError = {
                    Log.e("SAVE_TASKS", "Error creating document: ${it.message}")
                    Toast.makeText(this, "Failed to create document", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            // ✅ If document exists, just save
            taskApiService.saveAllTasks(
                documentId = documentId!!,
                taskMap = allTasks,
                onSuccess = {
                    Log.d("SAVE_TASKS", "Tasks saved to existing document.")
                },
                onError = {
                    Log.e("SAVE_TASKS", "Failed to save tasks: ${it.message}")
                    Toast.makeText(this, "Failed to save tasks", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun loadTasksFromServer(docId: String) {
        Log.d("LOAD_TASKS", "Starting task load for documentId = $docId")

        val taskApiService = TaskApiService(this)

        taskApiService.getAllTasks(
            documentId = docId,
            onSuccess = { taskMap ->
                Log.d("LOAD_TASKS", "Raw quadrant keys returned: ${taskMap.keys}")

                // Clear existing lists
                q1Tasks.clear()
                q2Tasks.clear()
                q3Tasks.clear()
                q4Tasks.clear()

                // Load new data
                q1Tasks.addAll(taskMap["q1_u_i"].orEmpty())
                q2Tasks.addAll(taskMap["q2_nu_i"].orEmpty())
                q3Tasks.addAll(taskMap["q3_u_ni"].orEmpty())
                q4Tasks.addAll(taskMap["q4_nu_ni"].orEmpty())

                // Notify adapters
                adapterQ1.notifyDataSetChanged()
                adapterQ2.notifyDataSetChanged()
                adapterQ3.notifyDataSetChanged()
                adapterQ4.notifyDataSetChanged()

                Log.d("LOAD_TASKS", "Tasks loaded successfully from document $docId")
            },
            onError = { error ->
                Log.e("LOAD_TASKS", "Failed to load tasks from server")
                Log.e("LOAD_TASKS", "Error message: ${error.message}")
                Log.e("LOAD_TASKS", "Status code: ${error.networkResponse?.statusCode}")
                Log.e("LOAD_TASKS", "Data: ${error.networkResponse?.data?.toString(Charsets.UTF_8)}")

                Toast.makeText(this, "Failed to load tasks", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun onDeleteTask(task: Task) {
        // Call the server to delete the task first
        taskApiService.deleteTask(
            taskId = task.id,
            onSuccess = {
                // If successful, remove it from the correct list and notify the adapter
                val list = when (task.quadrant) {
                    "q1_u_i" -> q1Tasks
                    "q2_nu_i" -> q2Tasks
                    "q3_u_ni" -> q3Tasks
                    "q4_nu_ni" -> q4Tasks
                    else -> return@deleteTask
                }

                val index = list.indexOfFirst { it.id == task.id }
                if (index != -1) {
                    list.removeAt(index)
                    getAdapterForQuadrant(task.quadrant).notifyItemRemoved(index)
                }
            },
            onError = { error ->
                Toast.makeText(this, "Failed to delete task: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun onToggleComplete(task: Task) {
        task.isCompleted = !task.isCompleted

        Handler(Looper.getMainLooper()).post {
            val adapter = getAdapterForQuadrant(task.quadrant)
            val list = when (task.quadrant) {
                "q1_u_i" -> q1Tasks
                "q2_nu_i" -> q2Tasks
                "q3_u_ni" -> q3Tasks
                "q4_nu_ni" -> q4Tasks
                else -> return@post
            }
            val index = list.indexOfFirst { it.id == task.id }
            if (index != -1) {
                adapter.notifyItemChanged(index)
            }
        }

        saveAllTasksToServer()
    }

    private fun getAdapterForQuadrant(quadrant: String): TaskAdapter {
        return when (quadrant) {
            "q1_u_i" -> adapterQ1
            "q2_nu_i" -> adapterQ2
            "q3_u_ni" -> adapterQ3
            "q4_nu_ni" -> adapterQ4
            else -> throw IllegalArgumentException("Unknown quadrant: $quadrant")
        }
    }
}
