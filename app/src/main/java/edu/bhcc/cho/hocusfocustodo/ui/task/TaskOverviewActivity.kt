package edu.bhcc.cho.hocusfocustodo.ui.task

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import edu.bhcc.cho.hocusfocustodo.R
import edu.bhcc.cho.hocusfocustodo.data.model.Task
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

    private lateinit var logoutButton: Button

    private val q1Tasks = mutableListOf<Task>()
    private val q2Tasks = mutableListOf<Task>()
    private val q3Tasks = mutableListOf<Task>()
    private val q4Tasks = mutableListOf<Task>()

    private lateinit var adapterQ1: TaskAdapter
    private lateinit var adapterQ2: TaskAdapter
    private lateinit var adapterQ3: TaskAdapter
    private lateinit var adapterQ4: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_overview)

        bindViews()
        setupRecyclerViews()
        setupListeners()
        populateDummyTasks()
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

        logoutButton = findViewById(R.id.button_logout)
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
            Toast.makeText(this, "Logged out (placeholder)", Toast.LENGTH_SHORT).show()
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
        }
    }

    private fun onDeleteTask(task: Task) {
        val list = when (task.quadrant) {
            "q1_u_i" -> q1Tasks
            "q2_nu_i" -> q2Tasks
            "q3_u_ni" -> q3Tasks
            "q4_nu_ni" -> q4Tasks
            else -> return
        }
        val index = list.indexOfFirst { it.id == task.id }
        if (index != -1) {
            list.removeAt(index)
            getAdapterForQuadrant(task.quadrant).notifyItemRemoved(index)
        }
    }

    private fun onToggleComplete(task: Task) {
        // Optional: Save task completion state
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

    private fun populateDummyTasks() {
        val dummyQ1 = listOf(
            Task(UUID.randomUUID().toString(), "Finish capstone", false, "q1_u_i"),
            Task(UUID.randomUUID().toString(), "Submit demo video", true, "q1_u_i")
        )
        val dummyQ2 = listOf(
            Task(UUID.randomUUID().toString(), "Buy snacks for group meeting", false, "q2_nu_i")
        )
        val dummyQ3 = listOf(
            Task(UUID.randomUUID().toString(), "Ask Rider to update README", false, "q3_u_ni")
        )
        val dummyQ4 = listOf(
            Task(UUID.randomUUID().toString(), "Rewatch cat video", false, "q4_nu_ni")
        )

        q1Tasks.addAll(dummyQ1)
        q2Tasks.addAll(dummyQ2)
        q3Tasks.addAll(dummyQ3)
        q4Tasks.addAll(dummyQ4)

        adapterQ1.notifyItemRangeInserted(0, dummyQ1.size)
        adapterQ2.notifyItemRangeInserted(0, dummyQ2.size)
        adapterQ3.notifyItemRangeInserted(0, dummyQ3.size)
        adapterQ4.notifyItemRangeInserted(0, dummyQ4.size)
    }
}