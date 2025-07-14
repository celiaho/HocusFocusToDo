package edu.bhcc.cho.hocusfocustodo.ui.task

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

        holder.text.text = task.text
        holder.checkBox.isChecked = task.isCompleted
        holder.text.alpha = if (task.isCompleted) 0.5f else 1f

        holder.dueDate.setText(task.dueDate)
        holder.dueDate.visibility = if (task.dueDate != null) View.VISIBLE else View.GONE

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            task.isCompleted = isChecked
            onToggleComplete(task)
            notifyItemChanged(position)
        }

        holder.deleteButton.setOnClickListener {
            onDelete(task)
            tasks.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, tasks.size)
        }
    }

    override fun getItemCount(): Int = tasks.size
}