package edu.bhcc.cho.hocusfocustodo.ui.task

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import edu.bhcc.cho.hocusfocustodo.R
import edu.bhcc.cho.hocusfocustodo.data.model.Task

class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onDelete: (Task) -> Unit,
    private val onToggleComplete: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.checkbox_complete)
        val text: TextView = view.findViewById(R.id.text_task)
        val deleteButton: ImageButton = view.findViewById(R.id.button_delete)
        val dueDate: EditText = view.findViewById(R.id.edit_due_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        // Set task text
        holder.text.text = task.text

        // Apply strikethrough and alpha for completed tasks
        if (task.isCompleted) {
            holder.text.paintFlags = holder.text.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.text.alpha = 0.5f
        } else {
            holder.text.paintFlags = holder.text.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.text.alpha = 1f
        }

        // Set due date
        holder.dueDate.setText(task.dueDate)
        holder.dueDate.visibility = if (task.dueDate != null) View.VISIBLE else View.GONE

        // Remove any existing listener before setting the checked state
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = task.isCompleted

        // Set new listener / Defer toggle callback to avoid RecyclerView layout crash
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            holder.checkBox.post {
                task.isCompleted = isChecked
                onToggleComplete(task.copy(isCompleted = isChecked)) // copy to avoid mutating here
            }
        }

        // Defer delete callback as well (and remove local notifyItemRemoved)
        holder.deleteButton.setOnClickListener {
            holder.deleteButton.post {
                onDelete(task)
            }
        }
    }

    override fun getItemCount(): Int = tasks.size
}