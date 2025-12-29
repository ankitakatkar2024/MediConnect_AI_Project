package com.example.mediconnect_ai

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediconnect_ai.database.ANCVisit
import com.example.mediconnect_ai.databinding.ItemAncVisitBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ANCVisitAdapter(
    private val onActionClick: (ANCVisit) -> Unit
) : ListAdapter<ANCVisit, ANCVisitAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAncVisitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val visit = getItem(position)
        holder.bind(visit)
    }

    inner class ViewHolder(private val binding: ItemAncVisitBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(visit: ANCVisit) {
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

            binding.tvVisitName.text = visit.visitName
            binding.tvDueDate.text = "Due Date: ${dateFormat.format(Date(visit.dueDate))}"

            // --- THE COLOR LOGIC ---
            val now = System.currentTimeMillis()
            // 2 weeks in milliseconds (buffer for overdue)
            val overdueThreshold = visit.dueDate + (14L * 24 * 60 * 60 * 1000)

            if (visit.isCompleted) {
                // CASE 1: COMPLETED (Green)
                binding.cardView.setCardBackgroundColor(Color.parseColor("#E8F5E9")) // Light Green
                binding.tvStatusBadge.text = "COMPLETED"
                binding.tvStatusBadge.background.setTint(Color.parseColor("#2E7D32")) // Dark Green
                binding.btnMarkDone.visibility = View.GONE

                binding.tvCompletionDate.visibility = View.VISIBLE
                val doneDate = visit.completionDate ?: now
                binding.tvCompletionDate.text = "Done on: ${dateFormat.format(Date(doneDate))}"

            } else if (now > overdueThreshold) {
                // CASE 2: OVERDUE (Red)
                binding.cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE")) // Light Red
                binding.tvStatusBadge.text = "OVERDUE"
                binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#C62828")) // Dark Red
                binding.btnMarkDone.visibility = View.VISIBLE
                binding.tvCompletionDate.visibility = View.GONE

            } else if (now >= visit.dueDate - (7L * 24 * 60 * 60 * 1000)) {
                // CASE 3: DUE SOON (Within 1 week) -> Yellow
                binding.cardView.setCardBackgroundColor(Color.parseColor("#FFFDE7")) // Light Yellow
                binding.tvStatusBadge.text = "DUE NOW"
                binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#F9A825")) // Dark Yellow
                binding.btnMarkDone.visibility = View.VISIBLE
                binding.tvCompletionDate.visibility = View.GONE
            } else {
                // CASE 4: FUTURE (White/Gray)
                binding.cardView.setCardBackgroundColor(Color.WHITE)
                binding.tvStatusBadge.text = "UPCOMING"
                binding.tvStatusBadge.setBackgroundColor(Color.GRAY)
                binding.btnMarkDone.visibility = View.GONE // Hide button if it's too early
                binding.tvCompletionDate.visibility = View.GONE
            }

            binding.btnMarkDone.setOnClickListener {
                onActionClick(visit)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ANCVisit>() {
        override fun areItemsTheSame(oldItem: ANCVisit, newItem: ANCVisit) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ANCVisit, newItem: ANCVisit) = oldItem == newItem
    }
}