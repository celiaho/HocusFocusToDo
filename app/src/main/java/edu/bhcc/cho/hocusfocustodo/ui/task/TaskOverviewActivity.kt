package edu.bhcc.cho.hocusfocustodo.ui.task

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import edu.bhcc.cho.hocusfocustodo.R

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

    // Example data models
    private val q1Tasks = mutableListOf<String>()
    private val q2Tasks = mutableListOf<String>()
    private val q3Tasks = mutableListOf<String>()
    private val q4Tasks = mutableListOf<String>()

    private lateinit var adapterQ1: SimpleTaskAdapter
    private lateinit var adapterQ2: SimpleTaskAdapter
    private lateinit var adapterQ3: SimpleTaskAdapter
    private lateinit var adapterQ4: SimpleTaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_overview)

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

        logoutButton = findViewById(R.id.button_logout)
    }

    private fun setupRecyclerViews() {
        adapterQ1 = SimpleTaskAdapter(q1Tasks)
        adapterQ2 = SimpleTaskAdapter(q2Tasks)
        adapterQ3 = SimpleTaskAdapter(q3Tasks)
        adapterQ4 = SimpleTaskAdapter(q4Tasks)

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
            // TODO: Add real logout logic
        }

        inputQ1.setOnEditorActionListener { v, actionId, event ->
            val text = inputQ1.text?.toString()?.trim()
            if (!text.isNullOrEmpty()) {
                q1Tasks.add(text)
                adapterQ1.notifyItemInserted(q1Tasks.size - 1)
                inputQ1.text = null
            }
            true
        }

        inputQ2.setOnEditorActionListener { v, actionId, event ->
            val text = inputQ2.text?.toString()?.trim()
            if (!text.isNullOrEmpty()) {
                q2Tasks.add(text)
                adapterQ2.notifyItemInserted(q2Tasks.size - 1)
                inputQ2.text = null
            }
            true
        }

        inputQ3.setOnEditorActionListener { v, actionId, event ->
            val text = inputQ3.text?.toString()?.trim()
            if (!text.isNullOrEmpty()) {
                q3Tasks.add(text)
                adapterQ3.notifyItemInserted(q3Tasks.size - 1)
                inputQ3.text = null
            }
            true
        }

        inputQ4.setOnEditorActionListener { v, actionId, event ->
            val text = inputQ4.text?.toString()?.trim()
            if (!text.isNullOrEmpty()) {
                q4Tasks.add(text)
                adapterQ4.notifyItemInserted(q4Tasks.size - 1)
                inputQ4.text = null
            }
            true
        }
    }
}