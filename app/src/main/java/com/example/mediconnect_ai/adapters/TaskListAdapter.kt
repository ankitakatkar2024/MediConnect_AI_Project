package com.example.mediconnect_ai.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mediconnect_ai.R
import com.example.mediconnect_ai.database.Task

class TaskListAdapter(
    private var tasks: List<Task>,
    private val onTaskStatusChanged: (Task) -> Unit
) : RecyclerView.Adapter<TaskListAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.cbTaskStatus)
        val description: TextView = itemView.findViewById(R.id.tvTaskDescription)
        val patientName: TextView = itemView.findViewById(R.id.tvPatientName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.description.text = task.taskDescription
        holder.patientName.text = "For: ${task.patientName}"
        holder.checkBox.isChecked = task.isCompleted

        // Apply strikethrough effect if task is completed
        updateStrikethrough(holder.description, task.isCompleted)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            task.isCompleted = isChecked
            updateStrikethrough(holder.description, isChecked)
            onTaskStatusChanged(task)
        }
    }

    private fun updateStrikethrough(textView: TextView, isCompleted: Boolean) {
        if (isCompleted) {
            textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    override fun getItemCount() = tasks.size
}